package com.coreoz.plume.admin.db.generated;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import jakarta.annotation.Generated;

import java.sql.Types;




/**
 * QAdminRolePermission is a Querydsl query type for AdminRolePermission
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QAdminRolePermission extends com.querydsl.sql.RelationalPathBase<AdminRolePermission> {

    private static final long serialVersionUID = -1687382184;

    public static final QAdminRolePermission adminRolePermission = new QAdminRolePermission("PLM_ROLE_PERMISSION");

    public final NumberPath<Long> idRole = createNumber("idRole", Long.class);

    public final StringPath permission = createString("permission");

    public final com.querydsl.sql.PrimaryKey<AdminRolePermission> constraint3 = createPrimaryKey(idRole, permission);

    public final com.querydsl.sql.ForeignKey<AdminRole> plmRolePermissionRole = createForeignKey(idRole, "ID");

    public QAdminRolePermission(String variable) {
        super(AdminRolePermission.class, forVariable(variable), "PUBLIC", "PLM_ROLE_PERMISSION");
        addMetadata();
    }

    public QAdminRolePermission(String variable, String schema, String table) {
        super(AdminRolePermission.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QAdminRolePermission(String variable, String schema) {
        super(AdminRolePermission.class, forVariable(variable), schema, "PLM_ROLE_PERMISSION");
        addMetadata();
    }

    public QAdminRolePermission(Path<? extends AdminRolePermission> path) {
        super(path.getType(), path.getMetadata(), "PUBLIC", "PLM_ROLE_PERMISSION");
        addMetadata();
    }

    public QAdminRolePermission(PathMetadata metadata) {
        super(AdminRolePermission.class, metadata, "PUBLIC", "PLM_ROLE_PERMISSION");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(idRole, ColumnMetadata.named("ID_ROLE").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(permission, ColumnMetadata.named("PERMISSION").withIndex(2).ofType(Types.VARCHAR).withSize(255).notNull());
    }

}

