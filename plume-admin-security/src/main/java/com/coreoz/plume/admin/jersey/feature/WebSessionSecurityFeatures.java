package com.coreoz.plume.admin.jersey.feature;

import com.coreoz.plume.admin.services.configuration.AdminSecurityConfigurationService;
import com.coreoz.plume.admin.websession.SessionFingerprintExtractor;
import com.coreoz.plume.admin.websession.WebSessionPermission;
import com.coreoz.plume.admin.websession.WebSessionSecurityConfig;
import com.coreoz.plume.admin.websession.WebSessionSigner;
import com.coreoz.plume.admin.websession.jersey.JerseySessionParser;
import com.coreoz.plume.jersey.security.permission.PermissionFeature;
import com.coreoz.plume.jersey.security.permission.PermissionRequestProvider;
import jakarta.annotation.Nonnull;
import jakarta.inject.Inject;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.DynamicFeature;
import jakarta.ws.rs.core.Feature;
import jakarta.ws.rs.core.FeatureContext;
import org.glassfish.jersey.InjectionManagerProvider;
import org.glassfish.jersey.internal.inject.AbstractBinder;
import org.glassfish.jersey.internal.inject.Binder;
import org.glassfish.jersey.internal.inject.InjectionManager;
import org.glassfish.jersey.process.internal.RequestScoped;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

/**
 * Registers one or more {@link WebSessionSecurityConfig} in Jersey:
 * <ul>
 *   <li>secures resources annotated with the access control annotation of each config,</li>
 *   <li>binds the session objects so they can be injected with {@code @Context MyWebSession}.</li>
 * </ul>
 * <pre>
 * config.register(WebSessionSecurityFeatures.of(
 *     WebSessionSecurityConfig.admin(),
 *     WebSessionSecurityConfig.of(
 *         MyWebSession.class, RestrictTo.class, RestrictTo::value, MyWebSession::getHashedFingerprint)
 * ));
 * </pre>
 * All the configs are handled by a single {@link DynamicFeature} and a single Jersey {@link Binder}:
 * Jersey deduplicates registered components by class, so registering one component per config would
 * silently ignore all but the first one.
 * <p>
 * The {@link WebSessionSigner} and the security configuration are injected lazily from the
 * {@link InjectionManager} at request time, so this feature can be registered before the Guice bridge
 * without any ordering constraint.
 */
public final class WebSessionSecurityFeatures implements Feature {

    private final List<WebSessionSecurityConfig> configs;
    private final LazyWebSessionServices services = new LazyWebSessionServices();

    private WebSessionSecurityFeatures(List<WebSessionSecurityConfig> configs) {
        this.configs = configs;
    }

    public static WebSessionSecurityFeatures of(WebSessionSecurityConfig... configs) {
        return new WebSessionSecurityFeatures(List.of(configs));
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public boolean configure(FeatureContext context) {
        InjectionManager injectionManager = InjectionManagerProvider.getInjectionManager(context);

        List<PermissionFeature<?>> permissionFeatures = new ArrayList<>();
        for (WebSessionSecurityConfig config : configs) {
            permissionFeatures.add(new PermissionFeature(
                new LazyPermissionRequestProvider(config, injectionManager, services),
                config.accessControlAnnotation(),
                config.permissionExtractor()
            ));
        }

        // A single DynamicFeature applying every config permission check.
        // Jersey deduplicates components by class, so one DynamicFeature per config would not work.
        context.register((DynamicFeature) (resourceInfo, featureContext) -> {
            for (PermissionFeature<?> permissionFeature : permissionFeatures) {
                permissionFeature.configure(resourceInfo, featureContext);
            }
        });

        // A single Binder binding every config session class.
        context.register(sessionBinder(injectionManager));
        return true;
    }

    private Binder sessionBinder(InjectionManager injectionManager) {
        return new AbstractBinder() {
            @Override
            @SuppressWarnings({"unchecked", "rawtypes"})
            protected void configure() {
                for (WebSessionSecurityConfig config : configs) {
                    bindFactory((Supplier<Object>) () -> currentSession(
                        config,
                        services,
                        injectionManager,
                        injectionManager.getInstance(ContainerRequestContext.class)
                    ))
                        .to((Class) config.sessionClass())
                        .in(RequestScoped.class);
                }
            }
        };
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static WebSessionPermission currentSession(WebSessionSecurityConfig config, LazyWebSessionServices services,
                                                       InjectionManager injectionManager, ContainerRequestContext requestContext) {
        WebSessionSigner webSessionSigner = services.webSessionSigner(injectionManager);
        boolean useFingerprintCookie = services.configurationService(injectionManager).sessionUseFingerprintCookie();
        return (WebSessionPermission) JerseySessionParser.currentSessionInformation(
            requestContext,
            webSessionSigner,
            (Class) config.sessionClass(),
            (SessionFingerprintExtractor) config.fingerprintExtractor(),
            useFingerprintCookie,
            config.fingerprintCookieName()
        );
    }

    /**
     * Holds the Guice provided services needed to parse a session.
     * <p>
     * They are injected lazily on the first request: the Guice bridge does not resolve programmatic
     * {@code InjectionManager.getInstance(...)} lookups, but it does resolve injection points.
     */
    private static final class LazyWebSessionServices {
        @Inject
        private WebSessionSigner webSessionSigner;
        @Inject
        private AdminSecurityConfigurationService configurationService;
        private volatile boolean injected;

        private void ensureInjected(InjectionManager injectionManager) {
            if (!injected) {
                synchronized (this) {
                    if (!injected) {
                        injectionManager.inject(this);
                        injected = true;
                    }
                }
            }
        }

        private WebSessionSigner webSessionSigner(InjectionManager injectionManager) {
            ensureInjected(injectionManager);
            return webSessionSigner;
        }

        private AdminSecurityConfigurationService configurationService(InjectionManager injectionManager) {
            ensureInjected(injectionManager);
            return configurationService;
        }
    }

    private record LazyPermissionRequestProvider(
        WebSessionSecurityConfig config,
        InjectionManager injectionManager,
        LazyWebSessionServices services
    ) implements PermissionRequestProvider {

        @Nonnull
        @Override
        public String userInformation(@Nonnull ContainerRequestContext requestContext) {
            WebSessionPermission session = currentSession(config, services, injectionManager, requestContext);
            return session == null ? "<no user connected>" : session.getUserName();
        }

        @Nonnull
        @Override
        public Collection<String> correspondingPermissions(@Nonnull ContainerRequestContext requestContext) {
            WebSessionPermission session = currentSession(config, services, injectionManager, requestContext);
            return session == null || session.getPermissions() == null
                ? List.of()
                : session.getPermissions();
        }
    }

}
