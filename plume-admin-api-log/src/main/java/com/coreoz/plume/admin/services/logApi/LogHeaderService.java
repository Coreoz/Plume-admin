package com.coreoz.plume.admin.services.logApi;


import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.coreoz.plume.admin.db.daos.LogHeaderDao;
import com.coreoz.plume.admin.db.generated.LogHeader;
import com.coreoz.plume.db.crud.CrudService;
import com.google.common.net.HttpHeaders;

@Singleton
public class LogHeaderService extends CrudService<LogHeader> {

    private LogHeaderDao logHeaderDao;

    @Inject
    public LogHeaderService(LogHeaderDao logHeaderDao) {
        super(logHeaderDao);
        this.logHeaderDao = logHeaderDao;
    }

    public List<LogHeader> findHeaders(Long idLogApi, HttpPart httpPart) {
    	return logHeaderDao.findHeadersByApi(idLogApi, httpPart.name());
    }

    public LogHeaderBean getHeaderForApi(Long idLogApi, HttpPart httpPart) {
        List<LogHeader> headers = findHeaders(idLogApi, httpPart);
        return new LogHeaderBean(
        	headers,
            guessResponseMimeType(headers).map(MimeType::getFormattingMode).orElse("")
        );
    }

    public void saveHeader(LogInterceptHeaderBean interceptedHeader, HttpPart httpPart, Long idLog) {
        LogHeader header = new LogHeader();
        header.setIdLogApi(idLog);
        header.setKey(interceptedHeader.getKey());
        header.setValue(interceptedHeader.getValue());
        header.setType(httpPart.name());
        save(header);
    }

    public Optional<MimeType> guessResponseMimeType(List<LogHeader> headers) {
    	return headers
	    	.stream()
	    	.filter(header -> header.getKey().toLowerCase().contains(HttpHeaders.CONTENT_TYPE.toLowerCase()))
	    	.findFirst()
	    	.flatMap(header -> Stream
    			.of(MimeType.values())
    			.filter(mimeType -> header.getValue().contains(mimeType.getMimeType()))
    			.findFirst()
	    	);
    }

}
