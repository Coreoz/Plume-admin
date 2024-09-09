package com.coreoz.plume.admin.db.generated;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QAdminUser is a Querydsl query type for AdminUser
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QAdminUser extends com.querydsl.sql.RelationalPathBase<AdminUser> {

    private static final long serialVersionUID = -740466690;

    public static final QAdminUser adminUser = new QAdminUser("PLM_USER");

    public final DateTimePath<java.time.LocalDateTime> creationDate = createDateTime("creationDate", java.time.LocalDateTime.class);

    public final StringPath email = createString("email");

    public final StringPath firstName = createString("firstName");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final NumberPath<Long> idRole = createNumber("idRole", Long.class);

    public final StringPath lastName = createString("lastName");

    public final SimplePath<byte[]> mfaUserHandle = createSimple("mfaUserHandle", byte[].class);

    public final StringPath password = createString("password");

    public final StringPath userName = createString("userName");

    public final com.querydsl.sql.PrimaryKey<AdminUser> primary = createPrimaryKey(id);

    public final com.querydsl.sql.ForeignKey<AdminRole> plmUserRole = createForeignKey(idRole, "id");

    public final com.querydsl.sql.ForeignKey<AdminUserMfa> _plmUserMfaUser = createInvForeignKey(id, "id_user");

    public QAdminUser(String variable) {
        super(AdminUser.class, forVariable(variable), "null", "PLM_USER");
        addMetadata();
    }

    public QAdminUser(String variable, String schema, String table) {
        super(AdminUser.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QAdminUser(String variable, String schema) {
        super(AdminUser.class, forVariable(variable), schema, "PLM_USER");
        addMetadata();
    }

    public QAdminUser(Path<? extends AdminUser> path) {
        super(path.getType(), path.getMetadata(), "null", "PLM_USER");
        addMetadata();
    }

    public QAdminUser(PathMetadata metadata) {
        super(AdminUser.class, metadata, "null", "PLM_USER");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(creationDate, ColumnMetadata.named("creation_date").withIndex(3).ofType(Types.TIMESTAMP).withSize(19).notNull());
        addMetadata(email, ColumnMetadata.named("email").withIndex(6).ofType(Types.VARCHAR).withSize(255).notNull());
        addMetadata(firstName, ColumnMetadata.named("first_name").withIndex(4).ofType(Types.VARCHAR).withSize(255).notNull());
        addMetadata(id, ColumnMetadata.named("id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(idRole, ColumnMetadata.named("id_role").withIndex(2).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(lastName, ColumnMetadata.named("last_name").withIndex(5).ofType(Types.VARCHAR).withSize(255).notNull());
        addMetadata(mfaUserHandle, ColumnMetadata.named("mfa_user_handle").withIndex(9).ofType(Types.LONGVARBINARY).withSize(65535));
        addMetadata(password, ColumnMetadata.named("password").withIndex(8).ofType(Types.VARCHAR).withSize(255).notNull());
        addMetadata(userName, ColumnMetadata.named("user_name").withIndex(7).ofType(Types.VARCHAR).withSize(255).notNull());
    }

}

