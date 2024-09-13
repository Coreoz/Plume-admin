package com.coreoz.plume.admin.db.generated;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import javax.annotation.processing.Generated;
import com.querydsl.sql.Column;

/**
 * AdminMfaBrowser is a Querydsl bean type
 */
@Generated("com.coreoz.plume.db.querydsl.generation.IdBeanSerializer")
public class AdminMfaBrowser extends com.coreoz.plume.db.querydsl.crud.CrudEntityQuerydsl {

    @Column("attestation")
    private byte[] attestation;

    @Column("client_data_json")
    private byte[] clientDataJson;

    @Column("id")
    @JsonSerialize(using=com.fasterxml.jackson.databind.ser.std.ToStringSerializer.class)
    private Long id;

    @Column("is_discoverable")
    private Boolean isDiscoverable;

    @Column("key_id")
    private byte[] keyId;

    @Column("public_key_cose")
    private byte[] publicKeyCose;

    @Column("signature_count")
    private Integer signatureCount;

    public byte[] getAttestation() {
        return attestation;
    }

    public void setAttestation(byte[] attestation) {
        this.attestation = attestation;
    }

    public byte[] getClientDataJson() {
        return clientDataJson;
    }

    public void setClientDataJson(byte[] clientDataJson) {
        this.clientDataJson = clientDataJson;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Boolean getIsDiscoverable() {
        return isDiscoverable;
    }

    public void setIsDiscoverable(Boolean isDiscoverable) {
        this.isDiscoverable = isDiscoverable;
    }

    public byte[] getKeyId() {
        return keyId;
    }

    public void setKeyId(byte[] keyId) {
        this.keyId = keyId;
    }

    public byte[] getPublicKeyCose() {
        return publicKeyCose;
    }

    public void setPublicKeyCose(byte[] publicKeyCose) {
        this.publicKeyCose = publicKeyCose;
    }

    public Integer getSignatureCount() {
        return signatureCount;
    }

    public void setSignatureCount(Integer signatureCount) {
        this.signatureCount = signatureCount;
    }

    @Override
    public boolean equals(Object o) {
        if (id == null) {
            return super.equals(o);
        }
        if (!(o instanceof AdminMfaBrowser)) {
            return false;
        }
        AdminMfaBrowser obj = (AdminMfaBrowser) o;
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

