package com.coreoz.plume.admin.db.daos;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.List;

import javax.inject.Inject;

import com.coreoz.plume.admin.db.generated.AdminMfaBrowser;
import com.coreoz.plume.admin.db.generated.AdminUser;
import com.coreoz.plume.admin.db.generated.AdminUserMfa;
import com.coreoz.plume.admin.db.generated.QAdminMfaBrowser;
import com.coreoz.plume.admin.db.generated.QAdminUser;
import com.coreoz.plume.admin.db.generated.QAdminUserMfa;
import com.coreoz.plume.db.querydsl.crud.CrudDaoQuerydsl;
import com.coreoz.plume.db.querydsl.transaction.TransactionManagerQuerydsl;
import com.google.inject.Singleton;
import com.yubico.webauthn.AssertionResult;
import com.yubico.webauthn.CredentialRepository;
import com.yubico.webauthn.RegisteredCredential;
import com.yubico.webauthn.RegistrationResult;
import com.yubico.webauthn.data.AuthenticatorAttestationResponse;
import com.yubico.webauthn.data.ByteArray;
import com.yubico.webauthn.data.ClientRegistrationExtensionOutputs;
import com.yubico.webauthn.data.PublicKeyCredential;
import com.yubico.webauthn.data.PublicKeyCredentialDescriptor;
import com.yubico.webauthn.data.PublicKeyCredentialType;
import com.yubico.webauthn.data.PublicKeyCredentialDescriptor.PublicKeyCredentialDescriptorBuilder;

@Singleton
public class AdminMfaBrowserCredentialDao implements CredentialRepository {

    private final TransactionManagerQuerydsl transactionManager;
    private final CrudDaoQuerydsl<AdminMfaBrowser> adminMfaBrowserDao;
    private final CrudDaoQuerydsl<AdminUserMfa> adminUserMfaDao;

    @Inject
	private AdminMfaBrowserCredentialDao(TransactionManagerQuerydsl transactionManager) {
		this.transactionManager = transactionManager;
        this.adminMfaBrowserDao = new CrudDaoQuerydsl<>(transactionManager, QAdminMfaBrowser.adminMfaBrowser);
        this.adminUserMfaDao = new CrudDaoQuerydsl<>(transactionManager, QAdminUserMfa.adminUserMfa);
	}

    public void registerCredential(
        AdminUser user,
        RegistrationResult result,
        PublicKeyCredential<AuthenticatorAttestationResponse, ClientRegistrationExtensionOutputs> pkc
    ) {
        AdminMfaBrowser mfa = new AdminMfaBrowser();
        mfa.setKeyId(result.getKeyId().getId().getBytes());
        mfa.setPublicKeyCose(result.getPublicKeyCose().getBytes());
        mfa.setSignatureCount((int)result.getSignatureCount());
        mfa.setIsDiscoverable(result.isDiscoverable().orElse(null));
        mfa.setAttestation(pkc.getResponse().getAttestationObject().getBytes());
        mfa.setClientDataJson(pkc.getResponse().getClientDataJSON().getBytes());
        adminMfaBrowserDao.save(mfa);

        AdminUserMfa userMfa = new AdminUserMfa();
        userMfa.setIdUser(user.getId());
        userMfa.setIdMfaBrowser(mfa.getId());
        userMfa.setType("Browser");
        adminUserMfaDao.save(userMfa);
    }

    public void updateCredential(
        AdminUser user,
        AssertionResult result
    ) {
        AdminMfaBrowser mfa = transactionManager.selectQuery()
            .select(QAdminMfaBrowser.adminMfaBrowser)
            .from(QAdminMfaBrowser.adminMfaBrowser)
            .join(QAdminUserMfa.adminUserMfa)
            .on(QAdminUserMfa.adminUserMfa.idMfaBrowser.eq(QAdminMfaBrowser.adminMfaBrowser.id))
            .join(QAdminUser.adminUser)
            .on(QAdminUser.adminUser.id.eq(QAdminUserMfa.adminUserMfa.idUser))
            .where(QAdminUser.adminUser.id.eq(user.getId())
                .and(QAdminMfaBrowser.adminMfaBrowser.keyId.eq(result.getCredentialId().getBytes())))
            .fetchOne();
        mfa.setSignatureCount((int)result.getSignatureCount());
        adminMfaBrowserDao.save(mfa);
    }

    @Override
    public Set<PublicKeyCredentialDescriptor> getCredentialIdsForUsername(String username) {
        List<byte[]> results = transactionManager.selectQuery()
            .select(QAdminMfaBrowser.adminMfaBrowser.publicKeyCose)
            .from(QAdminMfaBrowser.adminMfaBrowser)
            .join(QAdminUserMfa.adminUserMfa)
            .on(QAdminUserMfa.adminUserMfa.idMfaBrowser.eq(QAdminMfaBrowser.adminMfaBrowser.id))
            .join(QAdminUser.adminUser)
            .on(QAdminUser.adminUser.id.eq(QAdminUserMfa.adminUserMfa.idUser))
            .where(QAdminUser.adminUser.userName.eq(username))
            .fetch();
        // Transform the list of byte arrays into a set of PublicKeyCredentialDescriptors
        return results.stream()
            .map(bytes -> {
                PublicKeyCredentialDescriptorBuilder builder = PublicKeyCredentialDescriptor.builder()
                    .id(new ByteArray(bytes))
                    // Todo: everything should come from the database
                    .type(PublicKeyCredentialType.PUBLIC_KEY);
                return builder.build();
            })
            .collect(Collectors.toSet());
    }

    @Override
    public Optional<ByteArray> getUserHandleForUsername(String username) {
        byte[] bytes = transactionManager.selectQuery()
            .select(QAdminUser.adminUser.mfaUserHandle)
            .from(QAdminUser.adminUser)
            .where(QAdminUser.adminUser.userName.eq(username))
            .fetchOne();
        return Optional.ofNullable(bytes == null ? null : new ByteArray(bytes));
    }

    @Override
    public Optional<String> getUsernameForUserHandle(ByteArray userHandle) {
        return Optional.ofNullable(transactionManager.selectQuery()
            .select(QAdminUser.adminUser.userName)
            .from(QAdminUser.adminUser)
            .where(QAdminUser.adminUser.mfaUserHandle.eq(userHandle.getBytes()))
            .fetchOne());
    }

    @Override
    public Optional<RegisteredCredential> lookup(ByteArray credentialId, ByteArray userHandle) {
        AdminMfaBrowser mfa = transactionManager.selectQuery()
            .select(QAdminMfaBrowser.adminMfaBrowser)
            .from(QAdminMfaBrowser.adminMfaBrowser)
            .join(QAdminUserMfa.adminUserMfa)
            .on(QAdminUserMfa.adminUserMfa.idMfaBrowser.eq(QAdminMfaBrowser.adminMfaBrowser.id))
            .join(QAdminUser.adminUser)
            .on(QAdminUser.adminUser.id.eq(QAdminUserMfa.adminUserMfa.idUser))
            .where(QAdminUser.adminUser.mfaUserHandle.eq(userHandle.getBytes())
                .and(QAdminMfaBrowser.adminMfaBrowser.keyId.eq(credentialId.getBytes())))
            .fetchOne();
        if (mfa == null) {
            return Optional.empty();
        }
        return Optional.of(
            RegisteredCredential.builder()
                .credentialId(new ByteArray(mfa.getKeyId()))
                .userHandle(new ByteArray(userHandle.getBytes()))
                .publicKeyCose(new ByteArray(mfa.getPublicKeyCose()))
                .signatureCount(mfa.getSignatureCount())
            .build());
    }

    @Override
    public Set<RegisteredCredential> lookupAll(ByteArray credentialId) {
        List<AdminMfaBrowser> enrollements = transactionManager.selectQuery()
            .select(QAdminMfaBrowser.adminMfaBrowser)
            .from(QAdminMfaBrowser.adminMfaBrowser)
            .where(QAdminMfaBrowser.adminMfaBrowser.keyId.eq(credentialId.getBytes()))
            .fetch();
        // Convert to set
        return enrollements.stream()
            .map(mfa -> RegisteredCredential.builder()
                .credentialId(new ByteArray(mfa.getKeyId()))
                .userHandle(new ByteArray(mfa.getKeyId()))
                .publicKeyCose(new ByteArray(mfa.getPublicKeyCose()))
                .signatureCount(mfa.getSignatureCount())
                .build())
            .collect(Collectors.toSet());
    }

}
