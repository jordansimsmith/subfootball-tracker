package com.jordansimsmith.subfootballtracker.api.content;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "email")
public class EmailProperties {

    private String senderAddress;
    private String senderName;
    private String contentChangeTemplateId;
    private String sendGridApiKey;
    private Subscriber[] subscribers;

    public String getSenderAddress() {
        return senderAddress;
    }

    public void setSenderAddress(String senderAddress) {
        this.senderAddress = senderAddress;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getContentChangeTemplateId() {
        return contentChangeTemplateId;
    }

    public void setContentChangeTemplateId(String contentChangeTemplateId) {
        this.contentChangeTemplateId = contentChangeTemplateId;
    }

    public String getSendGridApiKey() {
        return sendGridApiKey;
    }

    public void setSendGridApiKey(String sendGridApiKey) {
        this.sendGridApiKey = sendGridApiKey;
    }

    public Subscriber[] getSubscribers() {
        return subscribers;
    }

    public void setSubscribers(Subscriber[] subscribers) {
        this.subscribers = subscribers;
    }
}