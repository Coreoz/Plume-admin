package com.coreoz.plume.admin.services.mfa;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.glassfish.jersey.internal.guava.Cache;
import org.glassfish.jersey.internal.guava.CacheBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.coreoz.plume.admin.db.daos.AdminMfaBrowserCredentialDao;
import com.coreoz.plume.admin.db.daos.AdminMfaDao;
import com.coreoz.plume.admin.db.daos.AdminUserDao;
import com.coreoz.plume.admin.db.generated.AdminMfaBrowser;
import com.coreoz.plume.admin.db.generated.AdminUser;
import com.coreoz.plume.admin.db.generated.AdminUserMfa;
import com.coreoz.plume.admin.services.configuration.AdminConfigurationService;
import com.coreoz.plume.admin.webservices.validation.AdminWsError;
import com.coreoz.plume.admin.websession.MfaSecretKeyEncryptionProvider;
import com.coreoz.plume.jersey.errors.WsException;
import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import com.yubico.webauthn.AssertionRequest;
import com.yubico.webauthn.AssertionResult;
import com.yubico.webauthn.FinishAssertionOptions;
import com.yubico.webauthn.FinishRegistrationOptions;
import com.yubico.webauthn.RegistrationResult;
import com.yubico.webauthn.RelyingParty;
import com.yubico.webauthn.StartAssertionOptions;
import com.yubico.webauthn.StartRegistrationOptions;
import com.yubico.webauthn.StartAssertionOptions.StartAssertionOptionsBuilder;
import com.yubico.webauthn.data.AuthenticatorAssertionResponse;
import com.yubico.webauthn.data.AuthenticatorAttestationResponse;
import com.yubico.webauthn.data.ByteArray;
import com.yubico.webauthn.data.ClientAssertionExtensionOutputs;
import com.yubico.webauthn.data.ClientRegistrationExtensionOutputs;
import com.yubico.webauthn.data.PublicKeyCredential;
import com.yubico.webauthn.data.PublicKeyCredentialCreationOptions;
import com.yubico.webauthn.data.RelyingPartyIdentity;
import com.yubico.webauthn.data.UserIdentity;
import com.yubico.webauthn.exception.AssertionFailedException;
import com.yubico.webauthn.exception.RegistrationFailedException;

@Singleton
public class MfaService {

    private static final Logger logger = LoggerFactory.getLogger(MfaService.class);

    private final GoogleAuthenticator authenticator = new GoogleAuthenticator();
    private final AdminConfigurationService configurationService;
    private final AdminMfaBrowserCredentialDao adminMfaBrowserCredentialDao;
    private final AdminUserDao adminUserDao;
    private final MfaSecretKeyEncryptionProvider mfaSecretKeyEncryptionProvider;
    private final RelyingParty relyingParty;
    private final Random random = new Random();
    private final Cache<Long, PublicKeyCredentialCreationOptions> createOptionCache =
        CacheBuilder.newBuilder().expireAfterAccess(2, TimeUnit.MINUTES).build();
    private final Cache<String, AssertionRequest> verifyOptionCache =
        CacheBuilder.newBuilder().expireAfterAccess(2, TimeUnit.MINUTES).build();

    @Inject
    private MfaService(
        AdminConfigurationService configurationService,
        MfaSecretKeyEncryptionProvider mfaSecretKeyEncryptionProvider,
        AdminMfaBrowserCredentialDao adminMfaBrowserCredentialDao,
        AdminUserDao adminUserDao
    ) {
        this.configurationService = configurationService;
        // TODO: Avoir un conf une liste des mfa à activer
        this.mfaSecretKeyEncryptionProvider = mfaSecretKeyEncryptionProvider;
        this.adminMfaBrowserCredentialDao = adminMfaBrowserCredentialDao;
        this.adminUserDao = adminUserDao;
        RelyingPartyIdentity identity = RelyingPartyIdentity.builder()
            .id("localhost") // TODO: Conf ?
            .name(configurationService.appName())
            .build();
        this.relyingParty = RelyingParty.builder()
            .identity(identity)
            .credentialRepository(adminMfaBrowserCredentialDao)
            .build();
    }

    // --------------------- Authenticator ---------------------

    public String generateSecretKey() throws Exception {
        GoogleAuthenticatorKey key = authenticator.createCredentials();
        return key.getKey();
    }

    public String hashSecretKey(String secretKey) throws Exception {
        return mfaSecretKeyEncryptionProvider.get().encrypt(secretKey);
    }

    public String getQRBarcodeURL(String user, String secret) {
        final String issuer = configurationService.appName();
        return String.format("otpauth://totp/%s:%s?secret=%s&issuer=%s", issuer, user, secret, issuer);
    }

    public byte[] generateQRCode(String user, String secret) {
        String qrBarcodeURL = getQRBarcodeURL(user, secret);
        try {
            return QRCodeGenerator.generateQRCodeImage(qrBarcodeURL, 200, 200);
        } catch (WriterException | IOException e) {
            logger.error("Error generating QR code", e);
            return null;
        }
    }

    public boolean verifyCode(String secret, int code) {
        try {
            return authenticator.authorize(mfaSecretKeyEncryptionProvider.get().decrypt(secret), code);
        } catch (Exception e) {
            logger.info("could not decrypt secret key", e);
            return false;
        }
    }

    private static class QRCodeGenerator {
        public static byte[] generateQRCodeImage(String barcodeText, int width, int height) throws WriterException, IOException {
            QRCodeWriter barcodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = barcodeWriter.encode(barcodeText, BarcodeFormat.QR_CODE, width, height);

            ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream);
            return pngOutputStream.toByteArray();
        }
    }

    // --------------------- Browser ---------------------

    public PublicKeyCredentialCreationOptions startRegistration(AdminUser user) {
        byte[] userHandle = new byte[64];
        random.nextBytes(userHandle);
        StartRegistrationOptions options = StartRegistrationOptions.builder()
            .user(UserIdentity.builder()
                .name(user.getUserName())
                .displayName(user.getUserName())
                .id(new ByteArray(userHandle))
                .build())
            .build();
        PublicKeyCredentialCreationOptions createOptions = relyingParty.startRegistration(options);
        createOptionCache.put(user.getId(), createOptions);
        return createOptions;
    }

    public boolean finishRegistration(
        AdminUser user,
        PublicKeyCredential<AuthenticatorAttestationResponse, ClientRegistrationExtensionOutputs> pkc
    ) {
        PublicKeyCredentialCreationOptions request = createOptionCache.getIfPresent(user.getId());
        if (request == null) {
            return false;
        }
        try {
            RegistrationResult result = relyingParty.finishRegistration(
                FinishRegistrationOptions.builder()
                    .request(request)
                    .response(pkc)
                    .build()
            );
            adminMfaBrowserCredentialDao.registerCredential(user, result, pkc);
            return true;
        } catch (RegistrationFailedException e) {
            logger.error("Error finishing registration", e);
            return false;
        }
    }

    public AdminUser verifyWebauth(PublicKeyCredential<AuthenticatorAssertionResponse, ClientAssertionExtensionOutputs> pkc) {
        if (pkc.getResponse().getUserHandle().isEmpty()) {
            return null;
        }
        Optional<String> username = adminMfaBrowserCredentialDao.getUsernameForUserHandle(pkc.getResponse().getUserHandle().get());
        if (username.isEmpty()) {
            return null;
        }
        AssertionRequest assertion = verifyOptionCache.getIfPresent(username.get());

        try {
            AssertionResult result = relyingParty.finishAssertion(FinishAssertionOptions.builder()
                    .request(assertion)  // The PublicKeyCredentialRequestOptions from startAssertion above
                    .response(pkc)
                    .build());
            if (result.isSuccess()) {
                AdminUser user = adminUserDao.findByUserName(username.get()).get();
                adminMfaBrowserCredentialDao.updateCredential(user, result);
                return adminUserDao.findByUserName(result.getUsername()).get();
            }
            return null;
        } catch (AssertionFailedException e) {
            return null;
        }
    }

    public AssertionRequest getAssertionRequest(String username) {
        AssertionRequest request = relyingParty.startAssertion(StartAssertionOptions.builder()
            .username(username)
            .build());

        verifyOptionCache.put(username, request);
        return request;
    }

}
