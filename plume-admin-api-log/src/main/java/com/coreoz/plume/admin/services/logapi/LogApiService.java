package com.coreoz.plume.admin.services.logapi;

import com.coreoz.plume.admin.db.daos.LogApiDao;
import com.coreoz.plume.admin.db.daos.LogApiTrimmed;
import com.coreoz.plume.admin.db.generated.LogApi;
import com.coreoz.plume.admin.db.generated.QLogApi;
import com.coreoz.plume.admin.services.configuration.LogApiConfigurationService;
import com.coreoz.plume.db.crud.CrudService;
import com.querydsl.core.Tuple;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Singleton
public class LogApiService extends CrudService<LogApi> {

    private static final Logger logger = LoggerFactory.getLogger(LogApiService.class);

    private final LogApiDao logApiDao;
    private final LogHeaderService logHeaderService;
    private final Clock clock;
    private final int bodyMaxCharsDisplayed;
    private final Duration cleaningMaxDuration;
    private final int cleaningMaxLogsPerApi;
    private final boolean saveToDatabase;

    private final BlockingQueue<LogInterceptApiBean> logsToBeSaved;

    @Inject
    public LogApiService(LogApiDao logApiDao, LogHeaderService logHeaderService,
                         LogApiConfigurationService configurationService, Clock clock) {
        super(logApiDao);
        this.logApiDao = logApiDao;
        this.logHeaderService = logHeaderService;
        this.clock = clock;

        this.bodyMaxCharsDisplayed = configurationService.bodyMaxCharsDisplayed().intValue();
        this.cleaningMaxDuration = configurationService.cleaningMaxDuration();
        this.cleaningMaxLogsPerApi = configurationService.cleaningMaxLogsPerApi();
        this.saveToDatabase = configurationService.saveToDatabase();

        this.logsToBeSaved = new LinkedBlockingQueue<>();
        if (saveToDatabase) {
            new Thread(this::insertWaitingLogs, "Http Log API Async saving").start();
        }
    }

    public List<LogApiTrimmed> fetchAllTrimmedLogs(
        Integer limit,
        String method,
        Integer statusCode,
        String apiName,
        String url,
        Instant startDate,
        Instant endDate
    ) {
        return logApiDao.fetchTrimmedLogs(limit, method, statusCode, apiName, url, startDate, endDate);
    }

    public LogApiBean fetchLogDetails(Long id) {
        Optional<Tuple> log = this.logApiDao.findByIdWithMaxLength(id, this.bodyMaxCharsDisplayed);
        if (!log.isPresent()) {
            return null;
        }
        Tuple tuple = log.get();
        HttpHeaders headerRequest = this.logHeaderService.getHeaderForApi(id, HttpPart.REQUEST);
        HttpHeaders headerResponse = this.logHeaderService.getHeaderForApi(id, HttpPart.RESPONSE);
        return new LogApiBean(
            tuple.get(QLogApi.logApi.id),
            tuple.get(QLogApi.logApi.apiName),
            tuple.get(QLogApi.logApi.url),
            tuple.get(QLogApi.logApi.date),
            tuple.get(QLogApi.logApi.method),
            tuple.get(QLogApi.logApi.statusCode),
            tuple.get(QLogApi.logApi.bodyRequest),
            tuple.get(QLogApi.logApi.bodyResponse),
            headerRequest,
            headerResponse,
            isTooLong(tuple.get(QLogApi.logApi.bodyRequest.length())),
            isTooLong(tuple.get(QLogApi.logApi.bodyResponse.length()))
        );
    }

    public Optional<HttpBodyPart> findBodyPart(Long id, boolean isRequest) {
        return Optional
            .ofNullable(findById(id))
            .map(log -> new HttpBodyPart(
                log.getApiName(),
                isRequest ? log.getBodyRequest() : log.getBodyResponse(),
                MimeType
                    .guessResponseMimeType(logHeaderService.findHeaders(
                        id,
                        isRequest ? HttpPart.REQUEST : HttpPart.RESPONSE
                    ))
                    .map(MimeType::getFileExtension)
                    .orElse("txt")
            ));
    }

    public LogApiFilters fetchAvailableFilters() {
        return this.logApiDao.fetchAvailableFilters();
    }

    public void saveLog(LogInterceptApiBean interceptedLog) {
        if (saveToDatabase) {
            logsToBeSaved.add(interceptedLog);
        }
    }

    private boolean isTooLong(Integer length) {
        return length != null && length > this.bodyMaxCharsDisplayed;
    }

    private void insertWaitingLogs() {
        while (true) {
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
                for (HttpHeader requestHeader : logToSave.getHeaderRequest()) {
                    logHeaderService.saveHeader(requestHeader, HttpPart.REQUEST, log.getId());
                }
                for (HttpHeader responseHeader : logToSave.getHeaderResponse()) {
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
    	long nbLogsDeleted = logApiDao.deleteOldLogs(clock.instant().minus(cleaningMaxDuration));
    	logger.debug("{} older logs than {}ms have been deleted", nbLogsDeleted, cleaningMaxDuration.toMillis());
    }

    private void deleteLogsOverApiLimit() {
    	for(String apiName : logApiDao.listApiNames()) {
    		long nbLogsDeleted = logApiDao.deleteLogsOverLimit(apiName, cleaningMaxLogsPerApi);
    		logger.debug("{} logs have been deleted for API {}", nbLogsDeleted, apiName);
    	}
    }

}
