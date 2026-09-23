package com.coreoz.plume.admin.websession.jersey;

import com.coreoz.plume.admin.websession.SessionFingerprintExtractor;
import com.coreoz.plume.admin.websession.WebSessionFingerprint;
import com.coreoz.plume.admin.websession.WebSessionPermission;
import com.coreoz.plume.admin.websession.WebSessionSigner;
import com.coreoz.plume.jersey.security.permission.PermissionFeature;
import com.coreoz.plume.jersey.security.permission.PermissionRequestProvider;
import jakarta.annotation.Nonnull;
import jakarta.ws.rs.container.ContainerRequestContext;

import java.util.Collection;
import java.util.List;

/**
 * Parse user information and permissions from a request. This is needed by {@link PermissionFeature}
 *
 * @param <T> A web session class implementing {@link WebSessionPermission}
 */
public class WebSessionRequestPermissionProvider<T extends WebSessionPermission> implements PermissionRequestProvider {

    private final WebSessionSigner webSessionSigner;
    private final Class<T> webSessionClass;
    private final SessionFingerprintExtractor<T> fingerprintExtractor;
    private final boolean verifyCookieFingerprint;
    private final String fingerprintCookieName;

    /**
     * If the session class implements {@link WebSessionFingerprint}, its fingerprint will be
     * verified when <code>verifyCookieFingerprint</code> is <code>true</code>.
     */
    public WebSessionRequestPermissionProvider(
        WebSessionSigner webSessionSigner,
        Class<T> webSessionClass,
        boolean verifyCookieFingerprint
    ) {
        this(
            webSessionSigner,
            webSessionClass,
            defaultFingerprintExtractor(webSessionClass),
            verifyCookieFingerprint,
            JerseySessionParser.FINGERPRINT_COOKIE_NAME
        );
    }

    public WebSessionRequestPermissionProvider(
        WebSessionSigner webSessionSigner,
        Class<T> webSessionClass,
        SessionFingerprintExtractor<T> fingerprintExtractor,
        boolean verifyCookieFingerprint
    ) {
        this(webSessionSigner, webSessionClass, fingerprintExtractor, verifyCookieFingerprint,
            JerseySessionParser.FINGERPRINT_COOKIE_NAME);
    }

    public WebSessionRequestPermissionProvider(
        WebSessionSigner webSessionSigner, Class<T> webSessionClass,
        SessionFingerprintExtractor<T> fingerprintExtractor,
        boolean verifyCookieFingerprint,
        String fingerprintCookieName
    ) {
        this.webSessionSigner = webSessionSigner;
        this.webSessionClass = webSessionClass;
        this.fingerprintExtractor = fingerprintExtractor;
        this.verifyCookieFingerprint = verifyCookieFingerprint;
        this.fingerprintCookieName = fingerprintCookieName;
    }

    @Nonnull
    @Override
    public String userInformation(
        @Nonnull ContainerRequestContext requestContext
    ) {
        WebSessionPermission session = currentSession(requestContext);
        return session == null ? "<no user connected>" : session.getUserName();
    }

    @Nonnull
    @Override
    public Collection<String> correspondingPermissions(
        @Nonnull ContainerRequestContext requestContext
    ) {
        WebSessionPermission session = currentSession(requestContext);
        return session == null || session.getPermissions() == null ?
            List.of()
            : session.getPermissions();
    }

    private T currentSession(ContainerRequestContext requestContext) {
        return JerseySessionParser.currentSessionInformation(
            requestContext,
            webSessionSigner,
            webSessionClass,
            fingerprintExtractor,
            verifyCookieFingerprint,
            fingerprintCookieName
        );
    }

    private static <T extends WebSessionPermission> SessionFingerprintExtractor<T> defaultFingerprintExtractor(
        Class<T> sessionClass
    ) {
        if (WebSessionFingerprint.class.isAssignableFrom(sessionClass)) {
            return session -> ((WebSessionFingerprint) session).getHashedFingerprint();
        }
        return null;
    }

}
