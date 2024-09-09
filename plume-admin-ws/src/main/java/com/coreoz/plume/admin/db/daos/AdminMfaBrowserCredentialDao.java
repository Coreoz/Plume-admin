package com.coreoz.plume.admin.db.daos;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.List;

import javax.inject.Inject;

import com.coreoz.plume.admin.db.generated.AdminMfa;
import com.coreoz.plume.admin.db.generated.QAdminMfa;
import com.coreoz.plume.admin.db.generated.QAdminUser;
import com.coreoz.plume.admin.db.generated.QAdminUserMfa;
import com.coreoz.plume.db.querydsl.transaction.TransactionManagerQuerydsl;
import com.yubico.webauthn.CredentialRepository;
import com.yubico.webauthn.RegisteredCredential;
import com.yubico.webauthn.data.AuthenticatorTransport;
import com.yubico.webauthn.data.ByteArray;
import com.yubico.webauthn.data.PublicKeyCredentialDescriptor;
import com.yubico.webauthn.data.PublicKeyCredentialType;
import com.yubico.webauthn.data.PublicKeyCredentialDescriptor.PublicKeyCredentialDescriptorBuilder;

public class AdminMfaBrowserCredentialDao implements CredentialRepository {

    private final TransactionManagerQuerydsl transactionManager;

    @Inject
	public AdminMfaBrowserCredentialDao(TransactionManagerQuerydsl transactionManager) {
		this.transactionManager = transactionManager;
	}

    @Override
    public Set<PublicKeyCredentialDescriptor> getCredentialIdsForUsername(String username) {
        List<byte[]> results = transactionManager.selectQuery()
            .select(QAdminMfa.adminMfa.credentialId)
            .from(QAdminMfa.adminMfa)
            .join(QAdminUserMfa.adminUserMfa)
            .on(QAdminUserMfa.adminUserMfa.idMfa.eq(QAdminMfa.adminMfa.id))
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
        String username = getUsernameForUserHandle(userHandle).orElse(null);
        if (username == null) {
            return Optional.empty();
        }
        AdminMfa mfa = transactionManager.selectQuery()
            .select(QAdminMfa.adminMfa)
            .from(QAdminMfa.adminMfa)
            .join(QAdminUserMfa.adminUserMfa)
            .on(QAdminUserMfa.adminUserMfa.idMfa.eq(QAdminMfa.adminMfa.id))
            .join(QAdminUser.adminUser)
            .on(QAdminUser.adminUser.id.eq(QAdminUserMfa.adminUserMfa.idUser))
            .where(QAdminMfa.adminMfa.credentialId.eq(credentialId.getBytes())
                .and(QAdminUser.adminUser.userName.eq(username)))
            .fetchOne();
        throw new UnsupportedOperationException("Unimplemented method 'lookup'");
    }

    @Override
    public Set<RegisteredCredential> lookupAll(ByteArray credentialId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'lookupAll'");
    }

}
