package com.coreoz.plume.admin.services.logApi;


import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.coreoz.plume.admin.db.daos.LogApiDao;
import com.coreoz.plume.admin.db.daos.LogApiTrimmed;
import com.coreoz.plume.admin.db.daos.LogHeaderDao;
import com.coreoz.plume.admin.db.generated.LogApi;
import com.coreoz.plume.admin.services.configuration.LogApiConfigurationService;
import com.coreoz.plume.db.crud.CrudService;

@Singleton
public class LogApiService extends CrudService<LogApi> {

    private LogApiDao logApiDao;
    private LogHeaderService logHeaderService;
    private LogApiConfigurationService configurationService;
    private LogHeaderDao logHeaderDao;

    @Inject
    public LogApiService(LogApiDao logApiDao, LogHeaderService logHeaderService, LogApiConfigurationService configurationService, LogHeaderDao logHeaderDao) {
        super(logApiDao);
        this.logApiDao = logApiDao;
        this.logHeaderService = logHeaderService;
        this.configurationService = configurationService;
        this.logHeaderDao = logHeaderDao;
    }

    public List<LogApiTrimmed> fetchAllTrimmedLogs() {
        return logApiDao.fetchTrimmedLogs();
    }

    public LogApiBean fetchLogDetails(Long id) {
        LogApi log = findById(id);
        LogHeaderBean headerRequest = logHeaderService.getHeaderForApi(id, HttpPart.REQUEST);
        LogHeaderBean headerResponse = logHeaderService.getHeaderForApi(id, HttpPart.RESPONSE);
        String bodyRequest = log.getBodyRequest();
        String bodyResponse = log.getBodyResponse();
        boolean isCompleteTextRequest = true;
        boolean isCompleteTextResponse = true;
        if (log.getBodyRequest().length() > this.configurationService.getLogBodyLimit()){
            isCompleteTextRequest = false;
            bodyRequest = bodyRequest.substring(0,this.configurationService.getLogBodyLimit());
        }
        if (log.getBodyResponse().length() > this.configurationService.getLogBodyLimit()){
            isCompleteTextResponse = false;
            bodyResponse = bodyResponse.substring(0,this.configurationService.getLogBodyLimit());
        }
        return new LogApiBean(
            log.getId(),
            log.getApi(),
            log.getUrl(),
            log.getDate(),
            log.getMethod(),
            log.getStatusCode(),
            bodyRequest,
            bodyResponse,
            headerRequest,
            headerResponse,
            isCompleteTextRequest,
            isCompleteTextResponse
        );
    }

	public Optional<HttpBodyPart> findBodyPart(Long id, Boolean isRequest) {
		return Optional
			.ofNullable(findById(id))
			.map(log -> new HttpBodyPart(
				log.getApi(),
				isRequest ? log.getBodyRequest() : log.getBodyResponse(),
				logHeaderService
					.guessResponseMimeType(logHeaderService.findHeaders(
						id,
						isRequest ? HttpPart.REQUEST : HttpPart.RESPONSE
					))
					.map(MimeType::getFileExtension)
					.orElse("txt")
			));
	}

    public void saveLog(LogInterceptApiBean interceptedLog){
        LogApi log = new LogApi();
        log.setDate(Instant.now());
        log.setMethod(interceptedLog.getMethod());
        log.setStatusCode(interceptedLog.getStatusCode());
        log.setUrl(interceptedLog.getUrl());
        log.setBodyRequest(interceptedLog.getBodyRequest());
        log.setBodyResponse(interceptedLog.getBodyResponse());
        log.setApi(interceptedLog.getApiName());
        Long logId = save(log).getId();
        interceptedLog.getHeaderRequest().forEach( header -> logHeaderService.saveHeader(header, HttpPart.REQUEST, logId));
        interceptedLog.getHeaderResponse().forEach( header -> logHeaderService.saveHeader(header, HttpPart.RESPONSE, logId));

    }
    public void cleanLogsNumberByApiName(){
        List<String> apiList = logApiDao.getListApiNames();
        apiList.forEach(this::deleteLogLinesOutsideLimit);
    }

    private void deleteLogLinesOutsideLimit(String apiName){
        List<LogApi> logListbyApi= logApiDao.getLogsbyApiName(apiName);
        if (logListbyApi.size() >= configurationService.getLogNumberMax()){
            List<LogApi> logsToBeDeletd = logListbyApi.stream().sorted(Comparator.comparing(LogApi::getDate)).limit(logListbyApi.size() - configurationService.getLogNumberMax()).collect(Collectors.toList());
            logsToBeDeletd.forEach(log -> this.deleteLog(log.getId()));
        }
    }

    public void deleteOldLogs(){
        List<LogApi> logOldList = logApiDao.getListApibyDate(configurationService.getLogNumberDaysLimit());
        logOldList.forEach(log -> this.deleteLog(log.getId()));
    }

    private void deleteLog(Long idLog){
        logHeaderDao.deleteHeadersbyApi(idLog);
        delete(idLog);
    }

}