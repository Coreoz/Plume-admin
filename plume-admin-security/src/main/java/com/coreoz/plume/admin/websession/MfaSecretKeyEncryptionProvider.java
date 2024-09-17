package com.coreoz.plume.admin.websession;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import com.coreoz.plume.admin.services.configuration.AdminSecurityConfigurationService;

@Singleton
public class MfaSecretKeyEncryptionProvider  implements Provider<MfaSecretKeyEncryption> {

    private final MfaSecretKeyEncryption mfaSecretKeyEncryption;

    @Inject
    private MfaSecretKeyEncryptionProvider(AdminSecurityConfigurationService conf) {
        this.mfaSecretKeyEncryption = new MfaSecretKeyEncryption(conf.mfaSecret());
    }

    @Override
    public MfaSecretKeyEncryption get() {
        return mfaSecretKeyEncryption;
    }
}
