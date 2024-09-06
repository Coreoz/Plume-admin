package com.coreoz.plume.admin.db.generated;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import javax.annotation.processing.Generated;
import com.querydsl.sql.Column;

/**
 * AdminUserMfa is a Querydsl bean type
 */
@Generated("com.coreoz.plume.db.querydsl.generation.IdBeanSerializer")
public class AdminUserMfa {

    @Column("ID_MFA")
    @JsonSerialize(using=com.fasterxml.jackson.databind.ser.std.ToStringSerializer.class)
    private Long idMfa;

    @Column("ID_USER")
    @JsonSerialize(using=com.fasterxml.jackson.databind.ser.std.ToStringSerializer.class)
    private Long idUser;

    public Long getIdMfa() {
        return idMfa;
    }

    public void setIdMfa(Long idMfa) {
        this.idMfa = idMfa;
    }

    public Long getIdUser() {
        return idUser;
    }

    public void setIdUser(Long idUser) {
        this.idUser = idUser;
    }

    @Override
    public String toString() {
        return "AdminUserMfa#" + idMfa+ ";" + idUser;
    }

    @Override
    public boolean equals(Object o) {
        if (idMfa == null || idUser == null) {
            return super.equals(o);
        }
        if (!(o instanceof AdminUserMfa)) {
            return false;
        }
        AdminUserMfa obj = (AdminUserMfa) o;
        return idMfa.equals(obj.idMfa) && idUser.equals(obj.idUser);
    }

    @Override
    public int hashCode() {
        if (idMfa == null || idUser == null) {
            return super.hashCode();
        }
        final int prime = 31;
        int result = 1;
        result = prime * result + idMfa.hashCode();
        result = prime * result + idUser.hashCode();
        return result;
    }

}

