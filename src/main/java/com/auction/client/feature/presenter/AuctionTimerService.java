package com.auction.client.feature.presenter;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

/**
 * Service to manage auction countdown timer
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
