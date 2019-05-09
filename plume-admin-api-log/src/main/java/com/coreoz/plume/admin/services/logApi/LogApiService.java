package com.coreoz.plume.admin.services.logApi;


import com.coreoz.plume.admin.db.daos.LogApiDao;
import com.coreoz.plume.admin.db.daos.LogHeaderDao;
import com.coreoz.plume.admin.db.generated.LogApi;
import com.coreoz.plume.admin.services.configuration.LogApiConfigurationService;
import com.coreoz.plume.db.crud.CrudService;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

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

    public List<LogApiBean> getAllLogs() {
        List<LogApi> logApiList = findAll();
        List<LogApiBean> logApiBeansList = new ArrayList<>();
        logApiList.forEach(log -> {
            logApiBeansList.add(getLogbyId(log.getId()));
        });
        return logApiBeansList.stream().sorted(Comparator.comparing(LogApiBean::getDate).reversed()).collect(Collectors.toList());
    }

    public LogApiBean getLogbyId(Long id){
        LogApi log = findById(id);
        LogHeaderBean headerRequest = logHeaderService.getHeaderForApi(id, "request");
        LogHeaderBean headerResponse = logHeaderService.getHeaderForApi(id, "response");
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
        interceptedLog.getHeaderRequest().forEach( header -> logHeaderService.saveHeader(header, "request", logId));
        interceptedLog.getHeaderResponse().forEach( header -> logHeaderService.saveHeader(header, "response", logId));

    }
    public void cleanLogsNumberByApiName(){
        List<String> apiList = logApiDao.getListApiNames();
        apiList.forEach(this::hasNumberofLogsReachedLimit);
    }

    private void hasNumberofLogsReachedLimit(String apiName){
        List<LogApi> logListbyApi= logApiDao.getLogsbyApiName(apiName);
        if (logListbyApi.size() >= configurationService.getLogNumberMax()){
            List<LogApi> logListSorted = logListbyApi.stream().sorted(Comparator.comparing(LogApi::getDate)).collect(Collectors.toList());
            List<LogApi> logsToBeDeletd = logListSorted.subList(0,logListbyApi.size() - configurationService.getLogNumberMax());
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