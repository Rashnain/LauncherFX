package com.github.rashnain.launcherfx.model;

import java.time.Instant;

public class MicrosoftAccount {

    private String username;
    private String uuid;
    private String accessToken;
    private String refreshToken;
    private String clientId;
    private String xuid;
    private Instant lastUsed;

    public MicrosoftAccount(String username, String uuid, String accessToken, String refreshToken, String clientId, String xuid, Instant lastUsed) {
        this.username = username;
        this.uuid = uuid;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.clientId = clientId;
        this.xuid = xuid;
        this.lastUsed = lastUsed;
    }

    public MicrosoftAccount() {
        this("", "", "", "", "", "", null);
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getXuid() {
        return xuid;
    }

    public void setXuid(String xuid) {
        this.xuid = xuid;
    }

    public Instant getLastUsed() {
        return lastUsed;
    }

    public void setLastUsed(Instant lastUsed) {
        this.lastUsed = lastUsed;
    }

}
