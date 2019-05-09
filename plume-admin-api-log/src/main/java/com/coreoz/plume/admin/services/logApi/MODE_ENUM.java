package com.coreoz.plume.admin.services.logApi;


public enum MODE_ENUM {
    XML("application/xml", "xml", "xml"),
    HTML("text/html", "htmlembedded", "html"),
    JSON("application/json", "javascript", "json"),
    TEXT("text/plain", "markdown", "txt");

    private String id;
    private String value;
    private String extension;

    MODE_ENUM(String id, String value, String extension) {
        this.id = id;
        this.value = value;
        this.extension = extension;
    }

    public String getId() {
        return id;
    }

    public String getValue() {
        return value;
    }

    public String getExtension() { return extension;}

}

