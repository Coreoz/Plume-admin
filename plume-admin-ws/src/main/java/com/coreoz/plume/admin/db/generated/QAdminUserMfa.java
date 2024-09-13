package com.coreoz.plume.admin.db.generated;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QAdminUserMfa is a Querydsl query type for AdminUserMfa
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QAdminUserMfa extends com.querydsl.sql.RelationalPathBase<AdminUserMfa> {

    private static final long serialVersionUID = -291052278;

    public static final QAdminUserMfa adminUserMfa = new QAdminUserMfa("PLM_USER_MFA");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final NumberPath<Long> idMfaAuthenticator = createNumber("idMfaAuthenticator", Long.class);

    public final NumberPath<Long> idMfaBrowser = createNumber("idMfaBrowser", Long.class);

    public final NumberPath<Long> idUser = createNumber("idUser", Long.class);

    public final StringPath type = createString("type");

    public final com.querydsl.sql.PrimaryKey<AdminUserMfa> primary = createPrimaryKey(id);

    public final com.querydsl.sql.ForeignKey<AdminMfaAuthenticator> plmUserMfaMfaAuthenticator = createForeignKey(idMfaAuthenticator, "id");

    public final com.querydsl.sql.ForeignKey<AdminMfaBrowser> plmUserMfaMfaBrowser = createForeignKey(idMfaBrowser, "id");

    public final com.querydsl.sql.ForeignKey<AdminUser> plmUserMfaUser = createForeignKey(idUser, "id");

    public QAdminUserMfa(String variable) {
        super(AdminUserMfa.class, forVariable(variable), "null", "PLM_USER_MFA");
        addMetadata();
    }

    public QAdminUserMfa(String variable, String schema, String table) {
        super(AdminUserMfa.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QAdminUserMfa(String variable, String schema) {
        super(AdminUserMfa.class, forVariable(variable), schema, "PLM_USER_MFA");
        addMetadata();
    }

    public QAdminUserMfa(Path<? extends AdminUserMfa> path) {
        super(path.getType(), path.getMetadata(), "null", "PLM_USER_MFA");
        addMetadata();
    }

    public QAdminUserMfa(PathMetadata metadata) {
        super(AdminUserMfa.class, metadata, "null", "PLM_USER_MFA");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(id, ColumnMetadata.named("id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(idMfaAuthenticator, ColumnMetadata.named("id_mfa_authenticator").withIndex(4).ofType(Types.BIGINT).withSize(19));
        addMetadata(idMfaBrowser, ColumnMetadata.named("id_mfa_browser").withIndex(5).ofType(Types.BIGINT).withSize(19));
        addMetadata(idUser, ColumnMetadata.named("id_user").withIndex(3).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(type, ColumnMetadata.named("type").withIndex(2).ofType(Types.VARCHAR).withSize(13).notNull());
    }

}

