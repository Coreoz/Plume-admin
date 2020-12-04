package com.coreoz.plume.admin.services.logApi;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.coreoz.plume.admin.db.daos.LogHeaderDao;
import com.coreoz.plume.admin.db.generated.LogHeader;
import com.coreoz.plume.db.crud.CrudService;

@Singleton
public class LogHeaderService extends CrudService<LogHeader> {

    private final LogHeaderDao logHeaderDao;

    @Inject
    public LogHeaderService(LogHeaderDao logHeaderDao) {
        super(logHeaderDao);
        this.logHeaderDao = logHeaderDao;
    }

    public List<LogHeader> findHeaders(Long idLogApi, HttpPart httpPart) {
    	return logHeaderDao.findHeadersByApi(idLogApi, httpPart.name());
    }

    HttpHeaders getHeaderForApi(Long idLogApi, HttpPart httpPart) {
        List<LogHeader> headers = findHeaders(idLogApi, httpPart);
        return new HttpHeaders(
        	headers,
        	MimeType.guessResponseMimeType(headers).map(MimeType::getMimeType).orElse("")
        );
    }

    void saveHeader(HttpHeader httpHeader, HttpPart httpPart, Long idLog) {
        LogHeader header = new LogHeader();
        header.setIdLogApi(idLog);
        header.setName(httpHeader.getName());
        header.setValue(httpHeader.getValue());
        header.setType(httpPart.name());

        logHeaderDao.save(header);
    }

}
