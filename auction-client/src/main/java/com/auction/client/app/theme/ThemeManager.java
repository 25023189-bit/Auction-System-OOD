package com.auction.client.app.theme;

import atlantafx.base.theme.PrimerLight;
import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;

import java.net.URL;

/**
 * Central place for applying the global JavaFX user-agent theme.
 */
public final class ThemeManager {
    private static final String PREMIUM_STYLESHEET =
            "/com/example/auctionprototype/css/auction-premium.css";

    private ThemeManager() {
    }

    public static void applyGlobalTheme() {
        Application.setUserAgentStylesheet(new PrimerLight().getUserAgentStylesheet());
    }

    public static Scene createThemedScene(Parent root) {
        Scene scene = new Scene(root);
        applyPremiumStylesheet(scene);
        return scene;
    }

    public static void applyPremiumStylesheet(Scene scene) {
        URL stylesheet = ThemeManager.class.getResource(PREMIUM_STYLESHEET);
        if (stylesheet != null) {
            scene.getStylesheets().add(stylesheet.toExternalForm());
        }
    }
}
