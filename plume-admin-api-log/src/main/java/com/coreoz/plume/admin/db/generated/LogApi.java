package com.coreoz.plume.admin.db.generated;
import javax.annotation.Generated;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.querydsl.sql.Column;

/**
 * LogApi is a Querydsl bean type
 */
@Generated("com.coreoz.plume.db.querydsl.generation.IdBeanSerializer")
public class LogApi extends com.coreoz.plume.db.querydsl.crud.CrudEntityQuerydsl {

    @Column("apiName")
    private String apiName;

    @Column("body_request")
    private String bodyRequest;

    @Column("body_response")
    private String bodyResponse;

    @Column("date")
    private java.time.Instant date;

    @JsonSerialize(using=com.fasterxml.jackson.databind.ser.std.ToStringSerializer.class)
    @Column("id")
    private Long id;

    @Column("method")
    private String method;

    @Column("status_code")
    private String statusCode;

    @Column("url")
    private String url;

    public String getApiName() {
        return apiName;
    }

    public void setApiName(String apiName) {
        this.apiName = apiName;
    }

    public String getBodyRequest() {
        return bodyRequest;
    }

    public void setBodyRequest(String bodyRequest) {
        this.bodyRequest = bodyRequest;
    }

    public String getBodyResponse() {
        return bodyResponse;
    }

    public void setBodyResponse(String bodyResponse) {
        this.bodyResponse = bodyResponse;
    }

    public java.time.Instant getDate() {
        return date;
    }

    public void setDate(java.time.Instant date) {
        this.date = date;
    }

    @Override
	public Long getId() {
        return id;
    }

    @Override
	public void setId(Long id) {
        this.id = id;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(String statusCode) {
        this.statusCode = statusCode;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public boolean equals(Object o) {
        if (id == null) {
            return super.equals(o);
        }
        if (!(o instanceof LogApi)) {
            return false;
        }
        LogApi obj = (LogApi) o;
        return id.equals(obj.id);
    }

    @Override
    public int hashCode() {
        if (id == null) {
            return super.hashCode();
        }
        final int prime = 31;
        int result = 1;
        result = prime * result + id.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "LogApi#" + id;
    }

}

