package com.coreoz.plume.admin.services.mfa;

public enum MfaTypeEnum {
    AUTHENTICATOR("authenticator"),
    BROWSER("browser");

    private final String type;

    MfaTypeEnum(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
