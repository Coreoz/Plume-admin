package com.coreoz.plume.admin.services.logApi;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
enum MimeType {
    XML("application/xml", "xml", "xml"),
    HTML("text/html", "htmlembedded", "html"),
    JSON("application/json", "javascript", "json"),
    TEXT("text/plain", "markdown", "txt");

    private final String mimeType;
    // TODO delete this field: the "mode" is related to a UI component, so it should be mapped in the UI
    private final String formattingMode;
    private final String fileExtension;

}
