package com.auction.server.handler;

import com.auction.common.dto.Message;
import com.auction.common.model.PendingAuctionRequest;
import com.auction.common.model.User;
import com.auction.server.dao.AuctionDAO;
import com.auction.server.dao.UserDAO;
import com.auction.server.service.AuctionCreationValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;

/**
 * Handler cho seller tạo yêu cầu mở phiên đấu giá.
 * Phiên mới không vào DB ngay mà được đưa vào hàng chờ admin phê duyệt.
 */
public class SellerActionHandler extends AbstractClientActionHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(SellerActionHandler.class);

    public SellerActionHandler() {
        super("CREATE_AUCTION");
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
                    generateId("AU", 6),
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
}
