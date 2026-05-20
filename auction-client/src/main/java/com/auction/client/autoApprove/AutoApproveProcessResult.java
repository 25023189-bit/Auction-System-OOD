package com.auction.client.autoApprove;

public class AutoApproveProcessResult {
    private final boolean timedOut;
    private final int exitCode;
    private final String output;

    private AutoApproveProcessResult(boolean timedOut, int exitCode, String output) {
        this.timedOut = timedOut;
        this.exitCode = exitCode;
        this.output = output == null ? "" : output;
    }

    public static AutoApproveProcessResult completed(int exitCode, String output) {
        return new AutoApproveProcessResult(false, exitCode, output);
    }

    public static AutoApproveProcessResult timedOut() {
        return new AutoApproveProcessResult(true, -1, "");
    }

    public boolean isTimedOut() {
        return timedOut;
    }

    public int getExitCode() {
        return exitCode;
    }

    public String getOutput() {
        return output;
    }
}
