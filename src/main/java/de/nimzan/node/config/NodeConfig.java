package de.nimzan.node.config;

public final class NodeConfig {
    private NodeConfig() {}

    public static String get(String key, String defaultValue) {
        // 1) ENV (ACTIVEMQ_BROKER_URL)
        String v = System.getenv(key);
        if (v != null && !v.isBlank()) return v;

        // 2) System properties (-DACTIVEMQ_BROKER_URL=...)
        v = System.getProperty(key);
        if (v != null && !v.isBlank()) return v;

        return defaultValue;
    }

    public static String brokerUrl() {
        return get("ACTIVEMQ_BROKER_URL", "tcp://localhost:61616");
    }

    public static String activemqUser() {
        return get("ACTIVEMQ_USER", "admin");
    }

    public static String activemqPassword() {
        return get("ACTIVEMQ_PASSWORD", "admin");
    }

    public static String masterUrl() {
        return get("MASTER_URL", "http://localhost:8080");
    }
}