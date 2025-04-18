package com.coreoz.plume.admin.db.generated;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.querydsl.sql.Column;
import jakarta.annotation.Generated;

/**
 * LogHeader is a Querydsl bean type
 */
@Generated("com.coreoz.plume.db.querydsl.generation.IdBeanSerializer")
public class LogHeader extends com.coreoz.plume.db.querydsl.crud.CrudEntityQuerydsl {

    @JsonSerialize(using=com.fasterxml.jackson.databind.ser.std.ToStringSerializer.class)
    @Column("id")
    private Long id;

    @JsonSerialize(using=com.fasterxml.jackson.databind.ser.std.ToStringSerializer.class)
    @Column("id_log_api")
    private Long idLogApi;

    @Column("name")
    private String name;

    @Column("type")
    private String type;

    @Column("value")
    private String value;

    @Override
	public Long getId() {
        return id;
    }

    @Override
	public void setId(Long id) {
        this.id = id;
    }

    public Long getIdLogApi() {
        return idLogApi;
    }

    public void setIdLogApi(Long idLogApi) {
        this.idLogApi = idLogApi;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (id == null) {
            return super.equals(o);
        }
        if (!(o instanceof LogHeader)) {
            return false;
        }
        LogHeader obj = (LogHeader) o;
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
        return "LogHeader#" + id;
    }

}

