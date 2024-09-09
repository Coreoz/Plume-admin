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

    public final NumberPath<Long> idMfa = createNumber("idMfa", Long.class);

    public final NumberPath<Long> idUser = createNumber("idUser", Long.class);

    public final com.querydsl.sql.PrimaryKey<AdminUserMfa> primary = createPrimaryKey(idMfa, idUser);

    public final com.querydsl.sql.ForeignKey<AdminMfa> plmUserMfaMfa = createForeignKey(idMfa, "id");

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
        addMetadata(idMfa, ColumnMetadata.named("id_mfa").withIndex(2).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(idUser, ColumnMetadata.named("id_user").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
    }

}

