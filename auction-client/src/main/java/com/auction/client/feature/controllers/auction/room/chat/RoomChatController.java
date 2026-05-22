package com.auction.client.feature.controllers.auction.room.chat;

import com.auction.client.feature.room.ChatActionHandler;
import com.auction.client.feature.room.ChatRequest;
import javafx.scene.control.TextField;

public class RoomChatController {
    private final ChatActionHandler chatActionHandler;
    private final TextField txtChatInput;

    public RoomChatController(ChatActionHandler chatActionHandler, TextField txtChatInput) {
        this.chatActionHandler = chatActionHandler;
        this.txtChatInput = txtChatInput;
    }

    public void handleSendChat() {
        if (chatActionHandler != null) {
            chatActionHandler.handle(new ChatRequest(readMessage()));
        }
        clearInput();
    }

    private String readMessage() {
        return txtChatInput == null || txtChatInput.getText() == null ? "" : txtChatInput.getText();
    }

    private void clearInput() {
        if (txtChatInput != null) {
            txtChatInput.clear();
        }
    }
}
