package com.auction.client.feature.presenter;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

/**
 * Service timer legacy gọi callback theo nhịp đếm ngược.
 *
 * Vai trò:
 * - Tạo Timeline chạy mỗi giây để gọi onTick.
 * - Gọi onComplete khi số chu kỳ timer kết thúc.
 *
 * Luồng chính:
 * 1. Caller set onTick/onComplete rồi gọi start(durationSeconds).
 * 2. Service stop timer cũ, tạo Timeline mới, chạy và hỗ trợ pause/resume/stop.
 *
 * Business rules:
 * - start() phải stop timer cũ trước để tránh callback chạy trùng.
 * - durationSeconds quyết định số lần tick trước khi hoàn tất.
 *
 * Ghi chú kỹ thuật:
 * - Không thread-safe: Timeline JavaFX phải thao tác trên JavaFX Application Thread.
 * - Dependency: Timeline, KeyFrame, Duration, Runnable.
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
