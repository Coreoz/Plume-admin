package com.coreoz.plume.admin.db.generated;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.querydsl.sql.Column;
import jakarta.annotation.Generated;

/**
 * AdminRolePermission is a Querydsl bean type
 */
@Generated("com.coreoz.plume.db.querydsl.generation.IdBeanSerializer")
public class AdminRolePermission {

    @Column("ID_ROLE")
    @JsonSerialize(using=com.fasterxml.jackson.databind.ser.std.ToStringSerializer.class)
    private Long idRole;

    @Column("PERMISSION")
    private String permission;

    public Long getIdRole() {
        return idRole;
    }

    public void setIdRole(Long idRole) {
        this.idRole = idRole;
    }

    public String getPermission() {
        return permission;
    }

    public void setPermission(String permission) {
        this.permission = permission;
    }

    @Override
    public boolean equals(Object o) {
        if (idRole == null || permission == null) {
            return super.equals(o);
        }
        if (!(o instanceof AdminRolePermission)) {
            return false;
        }
        AdminRolePermission obj = (AdminRolePermission) o;
        return idRole.equals(obj.idRole) && permission.equals(obj.permission);
    }

    @Override
    public int hashCode() {
        if (idRole == null || permission == null) {
            return super.hashCode();
        }
        final int prime = 31;
        int result = 1;
        result = prime * result + idRole.hashCode();
        result = prime * result + permission.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "AdminRolePermission#" + idRole+ ";" + permission;
    }

}

