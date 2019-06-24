package com.coreoz.plume.admin.services.logApi;


import com.coreoz.plume.admin.db.daos.LogHeaderDao;
import com.coreoz.plume.admin.db.generated.LogHeader;
import com.coreoz.plume.db.crud.CrudService;
import com.google.common.net.HttpHeaders;
import org.apache.commons.lang3.StringUtils;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.stream.Collectors;

@Singleton
public class LogHeaderService extends CrudService<LogHeader> {

    private LogHeaderDao logHeaderDao;

    @Inject
    public LogHeaderService(LogHeaderDao logHeaderDao) {
        super(logHeaderDao);
        this.logHeaderDao = logHeaderDao;
    }

    public LogHeaderBean getHeaderForApi(Long idApi, String type) {
        List<LogHeader> logHeaderList = logHeaderDao.findHeadersByApi(idApi, type);
        String mode = logHeaderList.stream().map(header -> {
            if(header.getKey().equals(HttpHeaders.CONTENT_TYPE)) {
                if (StringUtils.containsIgnoreCase(header.getValue(), MODE_ENUM.HTML.getId())) {
                    return MODE_ENUM.HTML.getValue();
                }
                if (header.getValue().contains(MODE_ENUM.JSON.getId())) {
                    return MODE_ENUM.JSON.getValue();
                }
                if (header.getValue().contains(MODE_ENUM.TEXT.getId())) {
                    return MODE_ENUM.TEXT.getValue();
                }
                if (header.getValue().contains(MODE_ENUM.XML.getId())) {
                    return MODE_ENUM.XML.getValue();
                }
                return "";
            }
            return "";
        }).collect(Collectors.joining());
        return new LogHeaderBean(
            logHeaderList,
            mode
        );
    }
    public String getMode(LogHeaderBean logHeader){
        if (logHeader.getMode().equals(MODE_ENUM.HTML.getValue())) {
            return MODE_ENUM.HTML.getExtension();
        }
        if (logHeader.getMode().equals(MODE_ENUM.JSON.getValue())) {
            return MODE_ENUM.JSON.getExtension();
        }
        if (logHeader.getMode().equals(MODE_ENUM.TEXT.getValue())) {
            return MODE_ENUM.TEXT.getExtension();
        }
        if (logHeader.getMode().equals(MODE_ENUM.XML.getValue())) {
            return MODE_ENUM.XML.getExtension();
        }
        return MODE_ENUM.TEXT.getExtension();
    }

    public void saveHeader(LogInterceptHeaderBean interceptedHeader, String type, Long idLog){
        LogHeader header = new LogHeader();
        header.setIdLogApi(idLog);
        header.setKey(interceptedHeader.getKey());
        header.setValue(interceptedHeader.getValue());
        header.setType(type);
        save(header);
    }
}


