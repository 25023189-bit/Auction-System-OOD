package com.auction.server.handler;

import com.auction.common.dto.Message;
import com.auction.common.model.AuctionRoom;
import com.auction.common.model.Item;
import com.auction.common.model.PendingAuctionRequest;
import com.auction.common.model.User;
import com.auction.server.dao.AuctionDAO;
import com.auction.server.dao.UserDAO;
import com.auction.server.service.AuctionCreationValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SellerActionHandler extends AbstractClientActionHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(SellerActionHandler.class);
    private static final Pattern EXTENSION_AND_IMAGE_PATTERN = Pattern.compile("^(\\d+)(.*)$");

    @FunctionalInterface
    public interface AutoApproveDecider {
        boolean shouldApprove(PendingAuctionRequest request);
    }

    private final AutoApproveDecider autoApproveDecider;
    private final PendingAuctionRoomFactory pendingAuctionRoomFactory;

    public SellerActionHandler() {
        this(request -> false, new PendingAuctionRoomFactory());
    }

    public SellerActionHandler(
            AutoApproveDecider autoApproveDecider,
            PendingAuctionRoomFactory pendingAuctionRoomFactory
    ) {
        super("CREATE_AUCTION");
        this.autoApproveDecider = Objects.requireNonNull(autoApproveDecider);
        this.pendingAuctionRoomFactory = Objects.requireNonNull(pendingAuctionRoomFactory);
    }

    @Override
    public void handle(Message message, ClientActionContext context) {
        handleCreateAuction(message, context);
    }

    private void handleCreateAuction(Message message, ClientActionContext context) {
        try {
            // Client gửi thông tin form theo format item|desc|price|minJoin|bidStep|start|duration|extension|base64Image.
            String[] parts = message.getData() != null
                    ? message.getData().toString().split("\\|", -1)
                    : new String[0];

            // Image is optional so old clients can still create auctions.
            if (parts.length < 8) {
                context.send(new Message("CREATE_AUCTION_FAIL", "SERVER", "Invalid auction creation data!"));
                return;
            }

            ParsedAuctionPayload payload = parsePayload(parts);
            String itemName = parts[0].trim();
            String itemDesc = parts[1].trim();
            double startingPrice = Double.parseDouble(parts[2].trim());
            double minimumJoinAmount = Double.parseDouble(parts[3].trim());
            double bidStep = Double.parseDouble(parts[4].trim());
            LocalDateTime startTime = LocalDateTime.parse(parts[5].trim());
            int durationMinutes = Integer.parseInt(parts[6].trim());
            int extensionSeconds = payload.extensionSeconds();

            // New clients send image as field 8; the parser also tolerates the current missing-delimiter payload.
            String base64Image = payload.base64Image();

            String sellerId = message.getId() != null ? message.getId().trim().toUpperCase() : "";
            UserDAO userDAO = new UserDAO();
            User seller = userDAO.getUserById(sellerId);
            if (seller == null) {
                context.send(new Message("CREATE_AUCTION_FAIL", "SERVER", "Seller account not found!"));
                return;
            }

            if (!"SELLER".equalsIgnoreCase(seller.getRole())) {
                context.send(new Message("CREATE_AUCTION_FAIL", "SERVER", "Only sellers can create auctions!"));
                return;
            }

            AuctionDAO auctionDAO = new AuctionDAO();
            AuctionDAO.SellerAuctionStats sellerStats = auctionDAO.getSellerAuctionStats(sellerId);
            seller.setSuccessfulAuctionRate(sellerStats.getSuccessfulAuctionRate());
            seller.setAdminCancellationRate(sellerStats.getAdminCancellationRate());

            AuctionCreationValidator validator = new AuctionCreationValidator();
            if (!validator.validateAuction(
                    sellerId,
                    seller.getOrganization(),
                    itemName,
                    itemDesc,
                    startingPrice,
                    minimumJoinAmount,
                    bidStep,
                    startTime,
                    durationMinutes,
                    extensionSeconds,
                    seller.getSellerReputation(),
                    seller.getSuccessfulAuctionRate(),
                    seller.getAdminCancellationRate()
            )) {
                context.send(new Message("CREATE_AUCTION_FAIL", "SERVER", validator.getErrorMessage()));
                return;
            }

            // 3. SỬA CHỖ NÀY: Truyền thêm base64Image vào cuối cùng của hàm khởi tạo
            PendingAuctionRequest request = new PendingAuctionRequest(
                    generateId("PA", 6),
                    auctionDAO.generateNextAuctionId(),
                    generateId("IT", 5),
                    sellerId,
                    seller.getOrganization(),
                    itemName,
                    itemDesc,
                    startingPrice,
                    minimumJoinAmount,
                    bidStep,
                    startTime,
                    durationMinutes,
                    extensionSeconds,
                    seller.getSellerReputation(),
                    seller.getSuccessfulAuctionRate(),
                    seller.getAdminCancellationRate(),
                    base64Image // <--- Gắn ảnh vào Request chờ duyệt
            );

            if (autoApproveDecider.shouldApprove(request)
                    && approveAutomatically(request, auctionDAO, context)) {
                return;
            }

            context.getPendingAuctionApprovalService().submit(request);
            context.send(new Message(
                    "CREATE_AUCTION_PENDING",
                    request.getRequestId(),
                    "Auction request submitted and is waiting for admin approval."
            ));
            broadcastPendingAuctionList(context);
        } catch (Exception e) {
            LOGGER.error("CREATE_AUCTION error.", e);
            context.send(new Message("CREATE_AUCTION_FAIL", "SERVER", "Error: " + e.getMessage()));
        }
    }

    private ParsedAuctionPayload parsePayload(String[] parts) {
        if (parts.length >= 9) {
            return new ParsedAuctionPayload(Integer.parseInt(parts[7].trim()), parts[8].trim());
        }

        String extensionAndMaybeImage = parts[7].trim();
        Matcher matcher = EXTENSION_AND_IMAGE_PATTERN.matcher(extensionAndMaybeImage);
        if (!matcher.matches()) {
            throw new NumberFormatException("Invalid extension seconds: " + extensionAndMaybeImage);
        }
        return new ParsedAuctionPayload(
                Integer.parseInt(matcher.group(1)),
                matcher.group(2).trim()
        );
    }

    private boolean approveAutomatically(
            PendingAuctionRequest request,
            AuctionDAO auctionDAO,
            ClientActionContext context
    ) {
        // Giữ nguyên đoạn này
        try {
            AuctionRoom room = pendingAuctionRoomFactory.createRoom(request);
            Item item = pendingAuctionRoomFactory.createItem(request);

            if (!auctionDAO.createAuctionWithItem(room, item, request.getSellerId())) {
                LOGGER.warn("Auto approve returned true, but database persistence failed for request {}.", request.getRequestId());
                return false;
            }

            AuctionImageRegistry.put(request.getRoomId(), request.getBase64Image());
            context.send(new Message("CREATE_AUCTION_SUCCESS", request.getRoomId(), "Auction auto-approved by AI."));
            broadcastRoomList(context);
            broadcastPendingAuctionList(context);
            return true;
        } catch (Exception e) {
            LOGGER.warn("Auto approve returned true, but approval handling failed for request {}.", request.getRequestId(), e);
            return false;
        }
    }

    private record ParsedAuctionPayload(int extensionSeconds, String base64Image) {
    }
}
