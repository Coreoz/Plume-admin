package com.coreoz.plume.admin.db.generated;

import static com.querydsl.core.types.PathMetadataFactory.forVariable;

import java.sql.Types;

import javax.annotation.Generated;

import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.sql.ColumnMetadata;




/**
 * QLogHeader is a Querydsl query type for LogHeader
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QLogHeader extends com.querydsl.sql.RelationalPathBase<LogHeader> {

    private static final long serialVersionUID = 1757724469;

    public static final QLogHeader logHeader = new QLogHeader("plm_log_header");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final NumberPath<Long> idLogApi = createNumber("idLogApi", Long.class);

    public final StringPath name = createString("name");

    public final StringPath type = createString("type");

    public final StringPath value = createString("value");

    public final com.querydsl.sql.PrimaryKey<LogHeader> primary = createPrimaryKey(id);

    public final com.querydsl.sql.ForeignKey<LogApi> plmLogHeaderPlmLogApiIdFk = createForeignKey(idLogApi, "id");

    public QLogHeader(String variable) {
        super(LogHeader.class, forVariable(variable), "null", "plm_log_header");
        addMetadata();
    }

    public QLogHeader(String variable, String schema, String table) {
        super(LogHeader.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QLogHeader(String variable, String schema) {
        super(LogHeader.class, forVariable(variable), schema, "plm_log_header");
        addMetadata();
    }

    public QLogHeader(Path<? extends LogHeader> path) {
        super(path.getType(), path.getMetadata(), "null", "plm_log_header");
        addMetadata();
    }

    public QLogHeader(PathMetadata metadata) {
        super(LogHeader.class, metadata, "null", "plm_log_header");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(id, ColumnMetadata.named("id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(idLogApi, ColumnMetadata.named("id_log_api").withIndex(2).ofType(Types.BIGINT).withSize(19));
        addMetadata(name, ColumnMetadata.named("name").withIndex(4).ofType(Types.VARCHAR).withSize(255));
        addMetadata(type, ColumnMetadata.named("type").withIndex(3).ofType(Types.VARCHAR).withSize(255));
        addMetadata(value, ColumnMetadata.named("value").withIndex(5).ofType(Types.VARCHAR).withSize(255));
    }

}

