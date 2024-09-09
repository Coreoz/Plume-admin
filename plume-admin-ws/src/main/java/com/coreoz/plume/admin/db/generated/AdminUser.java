package com.coreoz.plume.admin.db.generated;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import javax.annotation.processing.Generated;
import com.querydsl.sql.Column;

/**
 * AdminUser is a Querydsl bean type
 */
@Generated("com.coreoz.plume.db.querydsl.generation.IdBeanSerializer")
public class AdminUser extends com.coreoz.plume.db.querydsl.crud.CrudEntityQuerydsl {

    @Column("creation_date")
    private java.time.LocalDateTime creationDate;

    @Column("email")
    private String email;

    @Column("first_name")
    private String firstName;

    @Column("id")
    @JsonSerialize(using=com.fasterxml.jackson.databind.ser.std.ToStringSerializer.class)
    private Long id;

    @Column("id_role")
    @JsonSerialize(using=com.fasterxml.jackson.databind.ser.std.ToStringSerializer.class)
    private Long idRole;

    @Column("last_name")
    private String lastName;

    @Column("mfa_user_handle")
    private byte[] mfaUserHandle;

    @Column("password")
    private String password;

    @Column("user_name")
    private String userName;

    public java.time.LocalDateTime getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(java.time.LocalDateTime creationDate) {
        this.creationDate = creationDate;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getIdRole() {
        return idRole;
    }

    public void setIdRole(Long idRole) {
        this.idRole = idRole;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public byte[] getMfaUserHandle() {
        return mfaUserHandle;
    }

    public void setMfaUserHandle(byte[] mfaUserHandle) {
        this.mfaUserHandle = mfaUserHandle;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    @Override
    public boolean equals(Object o) {
        if (id == null) {
            return super.equals(o);
        }
        if (!(o instanceof AdminUser)) {
            return false;
        }
        AdminUser obj = (AdminUser) o;
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

}

