package com.jordansimsmith.subfootballtracker.api;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "email")
public record EmailProperties(
        String senderAddress,
        String senderName,
        String contentChangeTemplateId,
        String sendGridApiKey,
        Subscriber[] subscribers) {
    public record Subscriber(String name, String email) {}
}
