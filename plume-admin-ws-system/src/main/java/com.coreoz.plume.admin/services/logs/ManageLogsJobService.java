package com.coreoz.plume.admin.services.logs;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import com.coreoz.plume.admin.services.logs.bean.LogBean;

import com.coreoz.plume.admin.services.logs.enums.LogLevelEnum;
import org.slf4j.LoggerFactory;
import ch.qos.logback.classic.LoggerContext;

import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Singleton
public class ManageLogsJobService {

    private static Map<Integer, LogBean> logMap;

    public List<LogBean> getLogList() {
        List<LogBean> logList = new ArrayList<>();
        logMap = new HashMap<>();

        //get all Loggers in LoggerFactory
        List<Logger> listLogger = ((LoggerContext) LoggerFactory.getILoggerFactory()).getLoggerList();

        for (int i = 0; i < listLogger.size(); i++) {
            //Create newLog
            LogBean newLog = new LogBean();
            //Get Logger in the list from LoggerContext
            Logger loggerToFetch = listLogger.get(i);

            //Set newLog with logger values
            newLog.setId(i);
            newLog.setName(loggerToFetch.getName());
            if (loggerToFetch.getLevel() != null) {
                newLog.setLevel(loggerToFetch.getLevel().levelStr);
                newLog.setOldLevel(loggerToFetch.getLevel().levelStr);
            } else {
                newLog.setLevel(String.valueOf(LogLevelEnum.OFF));
                newLog.setOldLevel(String.valueOf(LogLevelEnum.OFF));
            }
            //Add to Log map
            logMap.put(i, newLog);
            logList.add(newLog);
        }

        return logList;
    }

    public void updateLog(Integer key, String level) {
        //Set real logger
        ((LoggerContext) LoggerFactory.getILoggerFactory()).getLoggerList().get(key).setLevel(Level.toLevel(level));

        //Get log from the log hashmap
        LogBean logToUpdate = logMap.get(key);
        //Set level to new level
        logToUpdate.setLevel(level);
        if (!level.toLowerCase().equals(String.valueOf(LogLevelEnum.OFF).toLowerCase())) {
            //If level to set is not 'off', set oldLevel
            logToUpdate.setOldLevel(level);
        }

        logMap.put(key, logToUpdate);
    }

    public void addLog(String name, String level) {
        //Getting non-existent logger name in LoggerFactory create and add it
        Logger logger = (Logger) LoggerFactory.getLogger(name);
        logger.setLevel(Level.valueOf(level));

    }

}