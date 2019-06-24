package com.coreoz.plume.admin.db.generated;
import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QLogApi is a Querydsl query type for LogApi
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QLogApi extends com.querydsl.sql.RelationalPathBase<LogApi> {

    private static final long serialVersionUID = 2131172978;

    public static final QLogApi logApi = new QLogApi("wlf_log_api");

    public final StringPath api = createString("api");

    public final StringPath bodyRequest = createString("bodyRequest");

    public final StringPath bodyResponse = createString("bodyResponse");

    public final DateTimePath<java.time.Instant> date = createDateTime("date", java.time.Instant.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath method = createString("method");

    public final StringPath statusCode = createString("statusCode");

    public final StringPath url = createString("url");

    public final com.querydsl.sql.PrimaryKey<LogApi> primary = createPrimaryKey(id);

    public final com.querydsl.sql.ForeignKey<LogHeader> _wlfLogHeaderWlfLogApiIdFk = createInvForeignKey(id, "id_log_api");

    public QLogApi(String variable) {
        super(LogApi.class, forVariable(variable), "null", "wlf_log_api");
        addMetadata();
    }

    public QLogApi(String variable, String schema, String table) {
        super(LogApi.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QLogApi(String variable, String schema) {
        super(LogApi.class, forVariable(variable), schema, "wlf_log_api");
        addMetadata();
    }

    public QLogApi(Path<? extends LogApi> path) {
        super(path.getType(), path.getMetadata(), "null", "plm_log_api");
        addMetadata();
    }

    public QLogApi(PathMetadata metadata) {
        super(LogApi.class, metadata, "null", "wlf_log_api");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(api, ColumnMetadata.named("api").withIndex(7).ofType(Types.VARCHAR).withSize(255));
        addMetadata(bodyRequest, ColumnMetadata.named("body_request").withIndex(5).ofType(Types.VARCHAR).withSize(255));
        addMetadata(bodyResponse, ColumnMetadata.named("body_response").withIndex(6).ofType(Types.VARCHAR).withSize(255));
        addMetadata(date, ColumnMetadata.named("date").withIndex(2).ofType(Types.TIMESTAMP).withSize(19));
        addMetadata(id, ColumnMetadata.named("id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(method, ColumnMetadata.named("method").withIndex(3).ofType(Types.VARCHAR).withSize(255));
        addMetadata(statusCode, ColumnMetadata.named("status_code").withIndex(4).ofType(Types.VARCHAR).withSize(255));
        addMetadata(url, ColumnMetadata.named("url").withIndex(8).ofType(Types.VARCHAR).withSize(255));
    }

}

