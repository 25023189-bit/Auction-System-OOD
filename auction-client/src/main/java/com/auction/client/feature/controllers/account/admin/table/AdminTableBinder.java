package com.auction.client.feature.controllers.account.admin.table;

import com.auction.common.model.AuctionRoom;
import com.auction.common.model.BidTransaction;
import com.auction.common.model.PendingAuctionRequest;
import com.auction.common.model.User;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.time.LocalDateTime;

public class AdminTableBinder {
    public void bindUsers(TableView<User> tableUsers,
                          TableColumn<User, String> colUserId,
                          TableColumn<User, String> colUsername,
                          TableColumn<User, String> colRole,
                          TableColumn<User, Double> colBalance,
                          ObservableList<User> users) {
        colUserId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colUsername.setCellValueFactory(new PropertyValueFactory<>("username"));
        colRole.setCellValueFactory(new PropertyValueFactory<>("role"));
        colBalance.setCellValueFactory(new PropertyValueFactory<>("balance"));
        tableUsers.setItems(users);
    }

    public void bindAuctions(TableView<AuctionRoom> tableAuctions,
                             TableColumn<AuctionRoom, String> colRoomId,
                             TableColumn<AuctionRoom, String> colRoomName,
                             TableColumn<AuctionRoom, String> colSeller,
                             TableColumn<AuctionRoom, String> colStatus,
                             TableColumn<AuctionRoom, Double> colPrice,
                             TableColumn<AuctionRoom, Double> colBidStep,
                             ObservableList<AuctionRoom> auctions) {
        colRoomId.setCellValueFactory(new PropertyValueFactory<>("roomId"));
        colRoomName.setCellValueFactory(new PropertyValueFactory<>("itemName"));
        colSeller.setCellValueFactory(new PropertyValueFactory<>("nameSeller"));
        colPrice.setCellValueFactory(new PropertyValueFactory<>("currentPrice"));
        colBidStep.setCellValueFactory(new PropertyValueFactory<>("bidStep"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        tableAuctions.setItems(auctions);
    }

    public void bindBidHistory(TableView<BidTransaction> tableBidHistory,
                               TableColumn<BidTransaction, String> colBidAuctionId,
                               TableColumn<BidTransaction, String> colBidderId,
                               TableColumn<BidTransaction, Double> colBidAmount,
                               TableColumn<BidTransaction, String> colBidTime,
                               ObservableList<BidTransaction> bidHistory) {
        colBidAuctionId.setCellValueFactory(new PropertyValueFactory<>("auctionId"));
        colBidderId.setCellValueFactory(new PropertyValueFactory<>("bidderId"));
        colBidAmount.setCellValueFactory(new PropertyValueFactory<>("bidAmount"));
        colBidTime.setCellValueFactory(new PropertyValueFactory<>("bidTime"));
        tableBidHistory.setItems(bidHistory);
    }

    public void bindPendingAuctions(TableView<PendingAuctionRequest> tablePendingAuctions,
                                    TableColumn<PendingAuctionRequest, String> colPendingRequestId,
                                    TableColumn<PendingAuctionRequest, String> colPendingSellerId,
                                    TableColumn<PendingAuctionRequest, String> colPendingSellerOrganization,
                                    TableColumn<PendingAuctionRequest, String> colPendingItemName,
                                    TableColumn<PendingAuctionRequest, String> colPendingItemDesc,
                                    TableColumn<PendingAuctionRequest, Double> colPendingStartingPrice,
                                    TableColumn<PendingAuctionRequest, Double> colPendingMinimumJoinAmount,
                                    TableColumn<PendingAuctionRequest, Double> colPendingBidStep,
                                    TableColumn<PendingAuctionRequest, Double> colPendingSellerReputation,
                                    TableColumn<PendingAuctionRequest, Double> colPendingSuccessfulAuctionRate,
                                    TableColumn<PendingAuctionRequest, Double> colPendingAdminCancellationRate,
                                    TableColumn<PendingAuctionRequest, LocalDateTime> colPendingStartTime,
                                    TableColumn<PendingAuctionRequest, Integer> colPendingDurationMinutes,
                                    TableColumn<PendingAuctionRequest, Integer> colPendingExtensionSeconds,
                                    ObservableList<PendingAuctionRequest> pendingAuctions) {
        colPendingRequestId.setCellValueFactory(new PropertyValueFactory<>("requestId"));
        colPendingSellerId.setCellValueFactory(new PropertyValueFactory<>("sellerId"));
        colPendingSellerOrganization.setCellValueFactory(new PropertyValueFactory<>("sellerOrganization"));
        colPendingItemName.setCellValueFactory(new PropertyValueFactory<>("itemName"));
        colPendingItemDesc.setCellValueFactory(new PropertyValueFactory<>("itemDesc"));
        colPendingStartingPrice.setCellValueFactory(new PropertyValueFactory<>("startingPrice"));
        colPendingMinimumJoinAmount.setCellValueFactory(new PropertyValueFactory<>("minimumJoinAmount"));
        colPendingBidStep.setCellValueFactory(new PropertyValueFactory<>("bidStep"));
        colPendingStartTime.setCellValueFactory(new PropertyValueFactory<>("startTime"));
        colPendingDurationMinutes.setCellValueFactory(new PropertyValueFactory<>("durationMinutes"));
        colPendingExtensionSeconds.setCellValueFactory(new PropertyValueFactory<>("extensionSeconds"));
        colPendingSellerReputation.setCellValueFactory(new PropertyValueFactory<>("sellerReputation"));
        colPendingSuccessfulAuctionRate.setCellValueFactory(new PropertyValueFactory<>("successfulAuctionRate"));
        colPendingAdminCancellationRate.setCellValueFactory(new PropertyValueFactory<>("adminCancellationRate"));
        tablePendingAuctions.setItems(pendingAuctions);
    }
}
