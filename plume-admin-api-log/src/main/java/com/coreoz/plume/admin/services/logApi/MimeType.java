package com.coreoz.plume.admin.services.logApi;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import com.coreoz.plume.admin.db.generated.LogHeader;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
enum MimeType {
    XML("application/xml", "xml"),
    ATOM_XML("application/atom+xml", "xml"),
    HTML("text/html", "html"),
    JSON("application/json", "json"),
    TEXT("text/plain", "txt");

    private final String mimeType;
    private final String fileExtension;

    static Optional<MimeType> guessResponseMimeType(List<LogHeader> headers) {
    	return headers
	    	.stream()
	    	.filter(header -> header.getName().toLowerCase().contains(com.google.common.net.HttpHeaders.CONTENT_TYPE.toLowerCase()))
	    	.findFirst()
	    	.flatMap(header -> Stream
    			.of(MimeType.values())
    			.filter(mimeType -> header.getValue().contains(mimeType.getMimeType()))
    			.findFirst()
	    	);
    }

}
