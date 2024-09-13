package com.coreoz.plume.admin.db.generated;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QAdminMfaBrowser is a Querydsl query type for AdminMfaBrowser
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QAdminMfaBrowser extends com.querydsl.sql.RelationalPathBase<AdminMfaBrowser> {

    private static final long serialVersionUID = -1158649325;

    public static final QAdminMfaBrowser adminMfaBrowser = new QAdminMfaBrowser("PLM_MFA_BROWSER");

    public final SimplePath<byte[]> attestation = createSimple("attestation", byte[].class);

    public final SimplePath<byte[]> clientDataJson = createSimple("clientDataJson", byte[].class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final BooleanPath isDiscoverable = createBoolean("isDiscoverable");

    public final SimplePath<byte[]> keyId = createSimple("keyId", byte[].class);

    public final SimplePath<byte[]> publicKeyCose = createSimple("publicKeyCose", byte[].class);

    public final NumberPath<Integer> signatureCount = createNumber("signatureCount", Integer.class);

    public final com.querydsl.sql.PrimaryKey<AdminMfaBrowser> primary = createPrimaryKey(id);

    public final com.querydsl.sql.ForeignKey<AdminUserMfa> _plmUserMfaMfaBrowser = createInvForeignKey(id, "id_mfa_browser");

    public QAdminMfaBrowser(String variable) {
        super(AdminMfaBrowser.class, forVariable(variable), "null", "PLM_MFA_BROWSER");
        addMetadata();
    }

    public QAdminMfaBrowser(String variable, String schema, String table) {
        super(AdminMfaBrowser.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QAdminMfaBrowser(String variable, String schema) {
        super(AdminMfaBrowser.class, forVariable(variable), schema, "PLM_MFA_BROWSER");
        addMetadata();
    }

    public QAdminMfaBrowser(Path<? extends AdminMfaBrowser> path) {
        super(path.getType(), path.getMetadata(), "null", "PLM_MFA_BROWSER");
        addMetadata();
    }

    public QAdminMfaBrowser(PathMetadata metadata) {
        super(AdminMfaBrowser.class, metadata, "null", "PLM_MFA_BROWSER");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(attestation, ColumnMetadata.named("attestation").withIndex(4).ofType(Types.LONGVARBINARY).withSize(65535).notNull());
        addMetadata(clientDataJson, ColumnMetadata.named("client_data_json").withIndex(5).ofType(Types.LONGVARBINARY).withSize(65535).notNull());
        addMetadata(id, ColumnMetadata.named("id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(isDiscoverable, ColumnMetadata.named("is_discoverable").withIndex(6).ofType(Types.BOOLEAN).withSize(3));
        addMetadata(keyId, ColumnMetadata.named("key_id").withIndex(2).ofType(Types.LONGVARBINARY).withSize(65535).notNull());
        addMetadata(publicKeyCose, ColumnMetadata.named("public_key_cose").withIndex(3).ofType(Types.LONGVARBINARY).withSize(65535).notNull());
        addMetadata(signatureCount, ColumnMetadata.named("signature_count").withIndex(7).ofType(Types.INTEGER).withSize(10).notNull());
    }

}

