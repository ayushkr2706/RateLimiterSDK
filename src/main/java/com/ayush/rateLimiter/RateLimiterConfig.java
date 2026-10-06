package com.ayush.rateLimiter;

import java.time.Duration;

public class RateLimiterConfig {

    private Duration connectTimeout = Duration.ofSeconds(1);
    private Duration requestTimeout = Duration.ofMillis(500);
    private boolean failOpen = true;

    public Duration getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(Duration connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public Duration getRequestTimeout() {
        return requestTimeout;
    }

    public void setRequestTimeout(Duration requestTimeout) {
        this.requestTimeout = requestTimeout;
    }

    public boolean isFailOpen() {
        return failOpen;
    }

    public void setFailOpen(boolean failOpen) {
        this.failOpen = failOpen;
    }
}
