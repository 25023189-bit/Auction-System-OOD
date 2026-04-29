package com.auction.client.feature.presenter;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

/**
 * Service đếm ngược phiên bản nhẹ, gọi callback onTick mỗi giây và onComplete khi kết thúc.
 */
public class AuctionTimerService {
    private Timeline timeline;
    private Runnable onTick;
    private Runnable onComplete;

    public AuctionTimerService() {
    }

    public void setOnTick(Runnable runnable) {
        this.onTick = runnable;
    }

    public void setOnComplete(Runnable runnable) {
        this.onComplete = runnable;
    }

    public void start(long durationSeconds) {
        // Dừng timer cũ trước khi tạo timer mới để tránh callback chạy trùng.
        stop();
        
        timeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            if (onTick != null) {
                onTick.run();
            }
        }));
        timeline.setCycleCount((int) durationSeconds);
        timeline.setOnFinished(event -> {
            if (onComplete != null) {
                onComplete.run();
            }
        });
        timeline.play();
    }

    public void stop() {
        if (timeline != null) {
            timeline.stop();
            timeline = null;
        }
    }

    public void pause() {
        if (timeline != null) {
            timeline.pause();
        }
    }

    public void resume() {
        if (timeline != null) {
            timeline.play();
        }
    }
}
