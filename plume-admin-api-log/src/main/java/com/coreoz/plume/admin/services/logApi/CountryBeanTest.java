package com.coreoz.plume.admin.services.logApi;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CountryBeanTest {
    private Integer id;
    private String revision;
    private String lastUpdate;
    private String updatedBy;
    private String code;
    private String name;
    private String timeZone;
    private ContinentBeanTest continent;

    public Integer getId() {
        return id;
    }

    public String getRevision() {
        return revision;
    }

    public String getLastUpdate() {
        return lastUpdate;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getTimeZone() {
        return timeZone;
    }

    public ContinentBeanTest getContinent() {
        return continent;
    }
}
