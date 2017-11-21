package com.codahale.metrics.riemann;

import java.net.InetAddress;

public enum HostnameResolution {
    LOCAL() {
        @Override
        public String getHostname() {
            try {
                return InetAddress.getLocalHost().getHostName();
            } catch (Exception e) {
                return null;
            }
        }
    },
    DOCKER {
        @Override
        public String getHostname() {
            return System.getenv("HOST");
        }
    };

    public abstract String getHostname();
}
