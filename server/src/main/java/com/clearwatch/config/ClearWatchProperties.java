package com.clearwatch.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "clearwatch")
public class ClearWatchProperties {

    private final Admin admin = new Admin();
    private final Jwt jwt = new Jwt();
    private final Simulator simulator = new Simulator();
    private final Retention retention = new Retention();
    private final Cors cors = new Cors();

    public Admin getAdmin() { return admin; }
    public Jwt getJwt() { return jwt; }
    public Simulator getSimulator() { return simulator; }
    public Retention getRetention() { return retention; }
    public Cors getCors() { return cors; }

    public static class Admin {
        private String email;
        private String password;
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

    public static class Jwt {
        private String secret;
        private long expirationMs;
        public String getSecret() { return secret; }
        public void setSecret(String secret) { this.secret = secret; }
        public long getExpirationMs() { return expirationMs; }
        public void setExpirationMs(long expirationMs) { this.expirationMs = expirationMs; }
    }

    public static class Simulator {
        private boolean enabled;
        private double speed;
        private int failureIntervalMinutes;
        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
        public double getSpeed() { return speed; }
        public void setSpeed(double speed) { this.speed = speed; }
        public int getFailureIntervalMinutes() { return failureIntervalMinutes; }
        public void setFailureIntervalMinutes(int failureIntervalMinutes) { this.failureIntervalMinutes = failureIntervalMinutes; }
    }

    public static class Retention {
        private int days;
        public int getDays() { return days; }
        public void setDays(int days) { this.days = days; }
    }

    public static class Cors {
        private String allowedOrigins;
        public String getAllowedOrigins() { return allowedOrigins; }
        public void setAllowedOrigins(String allowedOrigins) { this.allowedOrigins = allowedOrigins; }
    }
}
