package com.coreoz.plume.admin.services.logApi;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
enum MimeType {
    XML("application/xml", "xml"),
    HTML("text/html", "html"),
    JSON("application/json", "json"),
    TEXT("text/plain", "txt");

    private final String mimeType;
    private final String fileExtension;

}
