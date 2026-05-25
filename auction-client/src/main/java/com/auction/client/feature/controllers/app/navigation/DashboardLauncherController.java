package com.auction.client.feature.controllers.app.navigation;

import com.auction.client.app.launcher.AdminDashboardLauncher;
import com.auction.client.app.launcher.DashboardLauncher;
import com.auction.client.app.launcher.SellerDashboardLauncher;
import com.auction.client.service.AuctionService;
import com.auction.client.session.SessionStore;

public class DashboardLauncherController {
    private final DashboardLauncher sellerDashboardLauncher;
    private final DashboardLauncher adminDashboardLauncher;

    public DashboardLauncherController(AuctionService auctionService, SessionStore sessionStore) {
        sellerDashboardLauncher = new SellerDashboardLauncher(auctionService);
        adminDashboardLauncher = new AdminDashboardLauncher(auctionService, sessionStore);
    }

    public void openSellerDashboard() {
        sellerDashboardLauncher.launch();
    }

    public void openAdminDashboard() {
        adminDashboardLauncher.launch();
    }
}
