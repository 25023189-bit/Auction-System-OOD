package com.auction.client.feature.controllers.auction.notify;

import com.auction.common.dto.AuctionEndNotificationPayload;
import com.auction.common.dto.Message;
import com.auction.common.model.AuctionRoom;
import com.auction.common.model.User;

public class EndAuctionNotificationMapper {
    private static final String UNKNOWN = "Chua co thong tin";

    public EndAuctionNotificationViewModel map(Message message, User currentUser, AuctionRoom currentRoom) {
        AuctionEndNotificationPayload payload = extractPayload(message, currentRoom);
        String currentUserId = currentUser != null ? currentUser.getCustomerId() : null;
        String currentUsername = nonBlank(currentUser != null ? currentUser.getUsername() : null, UNKNOWN);
        String currentRole = nonBlank(currentUser != null ? currentUser.getRole() : null, UNKNOWN);
        AuctionEndReason endReason = parseReason(payload.getEndReason());
        EndAuctionResultType resultType = resolveResultType(payload, currentUserId, currentRole, endReason);

        TextBundle text = textFor(resultType);
        return new EndAuctionNotificationViewModel(
                nonBlank(payload.getAuctionId(), UNKNOWN),
                nonBlank(payload.getItemName(), UNKNOWN),
                payload.getFinalPrice(),
                resolveWinnerName(payload, currentUserId, currentUsername),
                currentUsername,
                currentRole,
                endReason,
                resultType,
                payload.isHasWinner(),
                payload.isTransactionApplied(),
                text.title(),
                text.body(),
                text.nextAction()
        );
    }

    private AuctionEndNotificationPayload extractPayload(Message message, AuctionRoom currentRoom) {
        Object data = message != null ? message.getData() : null;
        if (data instanceof AuctionEndNotificationPayload payload) {
            return payload;
        }

        AuctionEndNotificationPayload payload = new AuctionEndNotificationPayload();
        payload.setAuctionId(data != null ? data.toString() : currentRoom != null ? currentRoom.getRoomId() : null);
        payload.setItemName(currentRoom != null ? currentRoom.getItemName() : null);
        payload.setFinalPrice(currentRoom != null ? currentRoom.getCurrentPrice() : 0.0);
        payload.setWinnerId(currentRoom != null ? currentRoom.getHighestBidder() : null);
        payload.setHasWinner(currentRoom != null && currentRoom.getHighestBidder() != null && !currentRoom.getHighestBidder().isBlank());
        payload.setEndReason("UNKNOWN");
        payload.setFinalStatus(currentRoom != null ? currentRoom.getStatus() : null);
        payload.setTransactionApplied(false);
        return payload;
    }

    private EndAuctionResultType resolveResultType(
            AuctionEndNotificationPayload payload,
            String currentUserId,
            String currentRole,
            AuctionEndReason endReason
    ) {
        if (endReason == AuctionEndReason.SELLER_CLOSED) {
            return EndAuctionResultType.CLOSED_BY_SELLER;
        }
        if (endReason == AuctionEndReason.ADMIN_CLOSED || endReason == AuctionEndReason.ADMIN_BANNED) {
            return EndAuctionResultType.CLOSED_BY_ADMIN;
        }

        boolean seller = "SELLER".equalsIgnoreCase(currentRole);
        if (seller) {
            return payload.isHasWinner() ? EndAuctionResultType.SELLER_SOLD : EndAuctionResultType.SELLER_NO_WINNER;
        }

        if (!payload.isHasWinner()) {
            return EndAuctionResultType.NO_WINNER;
        }
        if (currentUserId != null && currentUserId.equals(payload.getWinnerId())) {
            return EndAuctionResultType.BIDDER_WIN;
        }
        return EndAuctionResultType.BIDDER_LOSE;
    }

    private AuctionEndReason parseReason(String rawReason) {
        if (rawReason == null || rawReason.isBlank()) {
            return AuctionEndReason.UNKNOWN;
        }
        try {
            return AuctionEndReason.valueOf(rawReason.trim().toUpperCase());
        } catch (IllegalArgumentException ignored) {
            return AuctionEndReason.UNKNOWN;
        }
    }

    private String resolveWinnerName(AuctionEndNotificationPayload payload, String currentUserId, String currentUsername) {
        if (!payload.isHasWinner()) {
            return "Khong co nguoi thang";
        }
        if (payload.getWinnerUsername() != null && !payload.getWinnerUsername().isBlank()) {
            return payload.getWinnerUsername();
        }
        if (currentUserId != null && currentUserId.equals(payload.getWinnerId())) {
            return currentUsername;
        }
        return UNKNOWN;
    }

    private TextBundle textFor(EndAuctionResultType resultType) {
        return switch (resultType) {
            case BIDDER_WIN -> new TextBundle(
                    "Chuc mung! Ban da thang phien dau gia.",
                    "Gia thang da duoc ghi nhan. Vui long theo doi trang thai thanh toan va giao dich.",
                    "Kiem tra so du va lich su giao dich neu can."
            );
            case BIDDER_LOSE -> new TextBundle(
                    "Phien dau gia da ket thuc. Ban chua thang phien nay.",
                    "Gia chot da thuoc ve nguoi tra gia cao nhat.",
                    "Quay lai lobby de xem cac phien dau gia khac."
            );
            case SELLER_SOLD -> new TextBundle(
                    "Phien dau gia da ket thuc voi nguoi thang.",
                    "San pham da duoc chot gia theo ket qua dau gia.",
                    "Kiem tra so du va xu ly buoc tiep theo voi nguoi mua."
            );
            case SELLER_NO_WINNER -> new TextBundle(
                    "Phien dau gia da ket thuc nhung khong co nguoi thang.",
                    "Khong co bid hop le duoc ghi nhan cho phien nay.",
                    "Ban co the tao lai phien hoac quay ve lobby."
            );
            case CLOSED_BY_SELLER -> new TextBundle(
                    "Phien da duoc nguoi ban ket thuc som.",
                    "Khong co giao dich nao duoc thuc hien cho lan dong phien nay.",
                    "Quay lai lobby de tiep tuc."
            );
            case CLOSED_BY_ADMIN -> new TextBundle(
                    "Phien da bi quan tri vien dong.",
                    "Khong co giao dich nao duoc thuc hien do phien bi can thiep.",
                    "Lien he quan tri vien neu can them thong tin."
            );
            case NO_WINNER -> new TextBundle(
                    "Phien dau gia da ket thuc.",
                    "Khong co nguoi thang hop le trong phien nay.",
                    "Quay lai lobby de xem phien khac."
            );
            case UNKNOWN -> new TextBundle(
                    "Phien dau gia da ket thuc.",
                    "He thong chua co du thong tin ket qua cho phien nay.",
                    "Quay lai lobby va lam moi danh sach phien."
            );
        };
    }

    private String nonBlank(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private record TextBundle(String title, String body, String nextAction) {
    }
}
