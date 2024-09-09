package com.coreoz.plume.admin.db.generated;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QAdminMfa is a Querydsl query type for AdminMfa
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QAdminMfa extends com.querydsl.sql.RelationalPathBase<AdminMfa> {

    private static final long serialVersionUID = 2054315861;

    public static final QAdminMfa adminMfa = new QAdminMfa("PLM_MFA");

    public final SimplePath<byte[]> credentialId = createSimple("credentialId", byte[].class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath secretKey = createString("secretKey");

    public final StringPath type = createString("type");

    public final com.querydsl.sql.PrimaryKey<AdminMfa> primary = createPrimaryKey(id);

    public final com.querydsl.sql.ForeignKey<AdminUserMfa> _plmUserMfaMfa = createInvForeignKey(id, "id_mfa");

    public QAdminMfa(String variable) {
        super(AdminMfa.class, forVariable(variable), "null", "PLM_MFA");
        addMetadata();
    }

    public QAdminMfa(String variable, String schema, String table) {
        super(AdminMfa.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QAdminMfa(String variable, String schema) {
        super(AdminMfa.class, forVariable(variable), schema, "PLM_MFA");
        addMetadata();
    }

    public QAdminMfa(Path<? extends AdminMfa> path) {
        super(path.getType(), path.getMetadata(), "null", "PLM_MFA");
        addMetadata();
    }

    public QAdminMfa(PathMetadata metadata) {
        super(AdminMfa.class, metadata, "null", "PLM_MFA");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(credentialId, ColumnMetadata.named("credential_id").withIndex(4).ofType(Types.LONGVARBINARY).withSize(65535));
        addMetadata(id, ColumnMetadata.named("id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(secretKey, ColumnMetadata.named("secret_key").withIndex(3).ofType(Types.VARCHAR).withSize(255));
        addMetadata(type, ColumnMetadata.named("type").withIndex(2).ofType(Types.VARCHAR).withSize(13).notNull());
    }

}

