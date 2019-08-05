package com.coreoz.plume.admin.services.logApi;


import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.coreoz.plume.admin.db.daos.LogApiDao;
import com.coreoz.plume.admin.db.daos.LogApiTrimmed;
import com.coreoz.plume.admin.db.generated.LogApi;
import com.coreoz.plume.admin.services.configuration.LogApiConfigurationService;
import com.coreoz.plume.db.crud.CrudService;
import com.coreoz.plume.services.time.TimeProvider;

@Singleton
public class LogApiService extends CrudService<LogApi> {

	private static final Logger logger = LoggerFactory.getLogger(LogApiService.class);

    private final LogApiDao logApiDao;
    private final LogHeaderService logHeaderService;
    private final TimeProvider timeProvider;
    private final int bodyMaxBytesDisplayed;
    private final Duration cleaningMaxDuration;
    private final int cleaningMaxLogsPerApi;

    private final BlockingQueue<LogInterceptApiBean> logsToBeSaved;

    @Inject
    public LogApiService(LogApiDao logApiDao, LogHeaderService logHeaderService,
    		LogApiConfigurationService configurationService, TimeProvider timeProvider) {
        super(logApiDao);
        this.logApiDao = logApiDao;
        this.logHeaderService = logHeaderService;
        this.timeProvider = timeProvider;

        this.bodyMaxBytesDisplayed = configurationService.bodyMaxBytesDisplayed().intValue();
        this.cleaningMaxDuration = configurationService.cleaningMaxDuration();
        this.cleaningMaxLogsPerApi = configurationService.cleaningMaxLogsPerApi();

        this.logsToBeSaved = new LinkedBlockingQueue<>();
        new Thread(this::insertWaitingLogs, "Http Log API Async saving").start();
    }

    public List<LogApiTrimmed> fetchAllTrimmedLogs() {
        return logApiDao.fetchTrimmedLogs();
    }

    public LogApiBean fetchLogDetails(Long id) {
        LogApi log = findById(id);
        HttpHeaders headerRequest = logHeaderService.getHeaderForApi(id, HttpPart.REQUEST);
        HttpHeaders headerResponse = logHeaderService.getHeaderForApi(id, HttpPart.RESPONSE);
        String bodyRequest = log.getBodyRequest();
        String bodyResponse = log.getBodyResponse();
        boolean isCompleteTextRequest = true;
        boolean isCompleteTextResponse = true;
        // TODO should be done in SQL directly
        if (log.getBodyRequest().length() > bodyMaxBytesDisplayed) {
            isCompleteTextRequest = false;
            bodyRequest = bodyRequest.substring(0, bodyMaxBytesDisplayed);
        }
        if (log.getBodyResponse().length() > bodyMaxBytesDisplayed) {
            isCompleteTextResponse = false;
            bodyResponse = bodyResponse.substring(0, bodyMaxBytesDisplayed);
        }
        return new LogApiBean(
            log.getId(),
            log.getApiName(),
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
				log.getApiName(),
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

    public void saveLog(LogInterceptApiBean interceptedLog) {
        logsToBeSaved.add(interceptedLog);
    }

    private void insertWaitingLogs() {
    	while(true) {
    		try {
				LogInterceptApiBean logToSave = logsToBeSaved.take();

		        LogApi log = new LogApi();
		        log.setDate(Instant.now());
		        log.setMethod(logToSave.getMethod());
		        log.setStatusCode(logToSave.getStatusCode());
		        log.setUrl(logToSave.getUrl());
		        log.setBodyRequest(logToSave.getBodyRequest());
		        log.setBodyResponse(logToSave.getBodyResponse());
		        log.setApiName(logToSave.getApiName());

	        	logApiDao.save(log);
	        	for(HttpHeader requestHeader : logToSave.getHeaderRequest()) {
	        		logHeaderService.saveHeader(requestHeader, HttpPart.REQUEST, log.getId());
	        	}
	        	for(HttpHeader responseHeader : logToSave.getHeaderResponse()) {
	        		logHeaderService.saveHeader(responseHeader, HttpPart.RESPONSE, log.getId());
	        	}
			} catch (Throwable e) {
				logger.error("Error saving HTTP log", e);
			}
    	}
    }

    // clean up

    public void cleanUp() {
    	deleteOldLogs();
    	deleteLogsOverApiLimit();
    }

    private void deleteOldLogs() {
    	long nbLogsDeleted = logApiDao.deleteOldLogs(timeProvider.currentInstant().minus(cleaningMaxDuration));
    	logger.debug("{} older logs than {}ms have been deleted", nbLogsDeleted, cleaningMaxDuration.toMillis());
    }

    private void deleteLogsOverApiLimit() {
    	for(String apiName : logApiDao.listApiNames()) {
    		long nbLogsDeleted = logApiDao.deleteLogsOverLimit(apiName, cleaningMaxLogsPerApi);
    		logger.debug("{} logs have been deleted for API {}", nbLogsDeleted, apiName);
    	}
    }

}