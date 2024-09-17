package com.coreoz.plume.admin.db.generated;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QAdminMfaAuthenticator is a Querydsl query type for AdminMfaAuthenticator
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QAdminMfaAuthenticator extends com.querydsl.sql.RelationalPathBase<AdminMfaAuthenticator> {

    private static final long serialVersionUID = 1997658142;

    public static final QAdminMfaAuthenticator adminMfaAuthenticator = new QAdminMfaAuthenticator("PLM_MFA_AUTHENTICATOR");

    public final SimplePath<byte[]> credentialId = createSimple("credentialId", byte[].class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath secretKey = createString("secretKey");

    public final com.querydsl.sql.PrimaryKey<AdminMfaAuthenticator> primary = createPrimaryKey(id);

    public final com.querydsl.sql.ForeignKey<AdminUserMfa> _plmUserMfaMfaAuthenticator = createInvForeignKey(id, "id_mfa_authenticator");

    public QAdminMfaAuthenticator(String variable) {
        super(AdminMfaAuthenticator.class, forVariable(variable), "null", "PLM_MFA_AUTHENTICATOR");
        addMetadata();
    }

    public QAdminMfaAuthenticator(String variable, String schema, String table) {
        super(AdminMfaAuthenticator.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QAdminMfaAuthenticator(String variable, String schema) {
        super(AdminMfaAuthenticator.class, forVariable(variable), schema, "PLM_MFA_AUTHENTICATOR");
        addMetadata();
    }

    public QAdminMfaAuthenticator(Path<? extends AdminMfaAuthenticator> path) {
        super(path.getType(), path.getMetadata(), "null", "PLM_MFA_AUTHENTICATOR");
        addMetadata();
    }

    public QAdminMfaAuthenticator(PathMetadata metadata) {
        super(AdminMfaAuthenticator.class, metadata, "null", "PLM_MFA_AUTHENTICATOR");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(credentialId, ColumnMetadata.named("credential_id").withIndex(3).ofType(Types.LONGVARBINARY).withSize(65535));
        addMetadata(id, ColumnMetadata.named("id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(secretKey, ColumnMetadata.named("secret_key").withIndex(2).ofType(Types.VARCHAR).withSize(255));
    }

}

