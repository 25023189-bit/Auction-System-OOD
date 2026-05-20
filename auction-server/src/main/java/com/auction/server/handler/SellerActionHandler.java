package com.auction.server.handler;

import com.auction.client.AI.auto_approve.AuctionAiAutoApproveConnector;
import com.auction.client.AI.auto_approve.AutoApproveListingInput;
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

/**
 * Handler xử lý yêu cầu seller tạo phiên đấu giá mới.
 *
 * Vai trò:
 * - Parse form tạo phiên từ client và xác thực seller hiện tại.
 * - Validate điều kiện tạo phiên rồi đưa request vào hàng chờ admin duyệt.
 *
 * Luồng chính:
 * 1. Nhận CREATE_AUCTION, tách payload item/giá/thời gian/extension và đọc seller từ DB.
 * 2. Chạy AuctionCreationValidator, tạo PendingAuctionRequest và broadcast danh sách pending cho admin.
 *
 * Business rules:
 * - Chỉ user role SELLER mới được tạo yêu cầu đấu giá.
 * - Phiên mới chưa ghi DB ngay; phải chờ admin approve trước khi trở thành auction thật.
 *
 * Ghi chú kỹ thuật:
 * - Không thread-safe theo instance; pending request dùng service với ConcurrentHashMap.
 * - Dependency: AbstractClientActionHandler, UserDAO, AuctionDAO, AuctionCreationValidator, PendingAuctionApprovalService.
 */
public class SellerActionHandler extends AbstractClientActionHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(SellerActionHandler.class);

    private final AuctionAiAutoApproveConnector autoApproveConnector;
    private final PendingAuctionRoomFactory pendingAuctionRoomFactory;

    public SellerActionHandler() {
        this(new AuctionAiAutoApproveConnector(), new PendingAuctionRoomFactory());
    }

    public SellerActionHandler(
            AuctionAiAutoApproveConnector autoApproveConnector,
            PendingAuctionRoomFactory pendingAuctionRoomFactory
    ) {
        super("CREATE_AUCTION");
        this.autoApproveConnector = Objects.requireNonNull(autoApproveConnector);
        this.pendingAuctionRoomFactory = Objects.requireNonNull(pendingAuctionRoomFactory);
    }

    @Override
    public void handle(Message message, ClientActionContext context) {
        handleCreateAuction(message, context);
    }

    private void handleCreateAuction(Message message, ClientActionContext context) {
        try {
            // Client gửi thông tin form theo format item|desc|price|minJoin|bidStep|start|duration|extension.
            String[] parts = message.getData() != null
                    ? message.getData().toString().split("\\|", -1)
                    : new String[0];

            if (parts.length < 8) {
                context.send(new Message("CREATE_AUCTION_FAIL", "SERVER", "Invalid auction creation data!"));
                return;
            }

            String itemName = parts[0].trim();
            String itemDesc = parts[1].trim();
            double startingPrice = Double.parseDouble(parts[2].trim());
            double minimumJoinAmount = Double.parseDouble(parts[3].trim());
            double bidStep = Double.parseDouble(parts[4].trim());
            LocalDateTime startTime = LocalDateTime.parse(parts[5].trim());
            int durationMinutes = Integer.parseInt(parts[6].trim());
            int extensionSeconds = Integer.parseInt(parts[7].trim());

            String sellerId = message.getId() != null ? message.getId().trim().toUpperCase() : "";
            UserDAO userDAO = new UserDAO();
            // Server luôn kiểm tra lại sellerId và role, không tin hoàn toàn dữ liệu client.
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
            // Thống kê seller là một phần điều kiện đánh giá yêu cầu tạo phiên.
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

            // Request chờ duyệt giữ đủ dữ liệu để admin approve mà không cần hỏi lại seller.
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
                    seller.getAdminCancellationRate()
            );

            if (autoApproveConnector.requestDecision(AutoApproveListingInput.fromPendingRequest(request))
                    && approveAutomatically(request, auctionDAO, context)) {
                return;
            }

            // Lưu request vào bộ nhớ server và báo client biết đang chờ admin.
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

    private boolean approveAutomatically(
            PendingAuctionRequest request,
            AuctionDAO auctionDAO,
            ClientActionContext context
    ) {
        try {
            AuctionRoom room = pendingAuctionRoomFactory.createRoom(request);
            Item item = pendingAuctionRoomFactory.createItem(request);

            if (!auctionDAO.createAuctionWithItem(room, item, request.getSellerId())) {
                LOGGER.warn(
                        "Auto approve returned true, but database persistence failed for request {}.",
                        request.getRequestId()
                );
                return false;
            }

            context.send(new Message(
                    "CREATE_AUCTION_SUCCESS",
                    request.getRoomId(),
                    "Auction auto-approved by AI."
            ));
            broadcastRoomList(context);
            broadcastPendingAuctionList(context);
            return true;
        } catch (Exception e) {
            LOGGER.warn(
                    "Auto approve returned true, but approval handling failed for request {}.",
                    request.getRequestId(),
                    e
            );
            return false;
        }
    }
}
