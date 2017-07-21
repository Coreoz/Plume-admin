package com.coreoz.plume.admin.db.generated;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QAdminRole is a Querydsl query type for AdminRole
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QAdminRole extends com.querydsl.sql.RelationalPathBase<AdminRole> {

    private static final long serialVersionUID = -740559703;

    public static final QAdminRole adminRole = new QAdminRole("PLM_ROLE");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath label = createString("label");

    public final com.querydsl.sql.PrimaryKey<AdminRole> constraintB = createPrimaryKey(id);

    public final com.querydsl.sql.ForeignKey<AdminRolePermission> _plmRolePermissionRole = createInvForeignKey(id, "ID_ROLE");

    public final com.querydsl.sql.ForeignKey<AdminUser> _plmUserRole = createInvForeignKey(id, "ID_ROLE");

    public QAdminRole(String variable) {
        super(AdminRole.class, forVariable(variable), "PUBLIC", "PLM_ROLE");
        addMetadata();
    }

    public QAdminRole(String variable, String schema, String table) {
        super(AdminRole.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QAdminRole(String variable, String schema) {
        super(AdminRole.class, forVariable(variable), schema, "PLM_ROLE");
        addMetadata();
    }

    public QAdminRole(Path<? extends AdminRole> path) {
        super(path.getType(), path.getMetadata(), "PUBLIC", "PLM_ROLE");
        addMetadata();
    }

    public QAdminRole(PathMetadata metadata) {
        super(AdminRole.class, metadata, "PUBLIC", "PLM_ROLE");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(id, ColumnMetadata.named("ID").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(label, ColumnMetadata.named("LABEL").withIndex(2).ofType(Types.VARCHAR).withSize(255).notNull());
    }

}

