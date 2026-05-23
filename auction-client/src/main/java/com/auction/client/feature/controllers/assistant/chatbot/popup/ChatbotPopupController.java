package com.auction.client.feature.controllers.assistant.chatbot.popup;

import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.stage.Popup;
import javafx.stage.Window;

import java.net.URL;

public class ChatbotPopupController {
    private static final String CHATBOT_FXML =
            "/com/example/auctionprototype/fxml/chatbot-chatting-view.fxml";

    /*
     * Shared popup/content để giữ lịch sử chat trong suốt vòng đời app.
     * Trước đây mỗi lần mở lại load FXML mới -> mất toàn bộ lịch sử.
     */
    private static Popup activePopup;
    private static Parent cachedContent;

    private static Window observedOwner;
    private static ChangeListener<Boolean> ownerFocusListener;
    private static ChangeListener<Boolean> ownerShowingListener;

    public void showChatbot(ActionEvent event) {
        Node source = event != null && event.getSource() instanceof Node node ? node : null;
        if (source == null || source.getScene() == null || source.getScene().getWindow() == null) {
            return;
        }

        try {
            show(source.getScene().getWindow(), activePopup);
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to open chatbot view.", ex);
        }
    }

    public void hideChatbot(ActionEvent event) {
        hideActivePopup();
    }

    public Popup show(Window owner, Popup currentPopup) throws Exception {
        if (owner == null) {
            return activePopup;
        }

        Popup popup = getOrCreatePopup();

        /*
         * Nếu đang mở mà bấm lại icon thì đóng.
         * Không tạo popup mới để tránh mất lịch sử.
         */
        if (popup.isShowing()) {
            popup.hide();
            return popup;
        }

        popup.setAutoHide(true);
        popup.setHideOnEscape(true);
        popup.setAutoFix(true);

        double x = owner.getX() + owner.getWidth() - 430;
        double y = owner.getY() + owner.getHeight() - 570;

        popup.show(owner, x, y);
        activePopup = popup;

        observeOwner(owner);

        return popup;
    }

    private static Popup getOrCreatePopup() throws Exception {
        if (activePopup != null && cachedContent != null) {
            return activePopup;
        }

        URL fxml = ChatbotPopupController.class.getResource(CHATBOT_FXML);
        if (fxml == null) {
            throw new IllegalStateException("Missing chatbot-chatting-view.fxml.");
        }

        cachedContent = new FXMLLoader(fxml).load();

        Popup popup = new Popup();
        popup.getContent().setAll(cachedContent);

        activePopup = popup;
        return popup;
    }

    private static void hideActivePopup() {
        if (activePopup != null && activePopup.isShowing()) {
            activePopup.hide();
        }
    }

    private static void observeOwner(Window owner) {
        detachOwnerListeners();

        observedOwner = owner;

        /*
         * Khi người dùng chuyển sang cửa sổ khác, ẩn popup.
         * Điều này ngăn ChatBot nổi đè lên Chrome/VSCode/app khác.
         */
        ownerFocusListener = (observable, oldValue, focused) -> {
            if (!focused) {
                hideActivePopup();
            }
        };

        /*
         * Khi main window bị đóng/ẩn/chuyển lifecycle, popup cũng ẩn theo.
         */
        ownerShowingListener = (observable, oldValue, showing) -> {
            if (!showing) {
                hideActivePopup();
            }
        };

        observedOwner.focusedProperty().addListener(ownerFocusListener);
        observedOwner.showingProperty().addListener(ownerShowingListener);
    }

    private static void detachOwnerListeners() {
        if (observedOwner == null) {
            return;
        }

        if (ownerFocusListener != null) {
            observedOwner.focusedProperty().removeListener(ownerFocusListener);
        }

        if (ownerShowingListener != null) {
            observedOwner.showingProperty().removeListener(ownerShowingListener);
        }

        observedOwner = null;
        ownerFocusListener = null;
        ownerShowingListener = null;
    }
}