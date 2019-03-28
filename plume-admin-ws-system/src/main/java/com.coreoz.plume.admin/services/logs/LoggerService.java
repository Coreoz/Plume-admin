package com.coreoz.plume.admin.services.logs;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import com.coreoz.plume.admin.services.logs.bean.LoggerLevel;

import org.slf4j.LoggerFactory;
import ch.qos.logback.classic.LoggerContext;

import javax.inject.Singleton;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Singleton
public class LoggerService {

    private static Map<String, String> logOriginalLevelMap = new HashMap<>();

    public List<LoggerLevel> getLogList() {
        return ((LoggerContext) LoggerFactory.getILoggerFactory()).getLoggerList()
            .stream()
            .map(logger -> new LoggerLevel(
                logger.getName(),
                logger.getLevel() != null && !logger.getLevel().equals(Level.OFF) ? logger.getLevel().levelStr : "",
                logOriginalLevelMap.getOrDefault(logger.getName(), logger.getLevel() != null ? logger.getLevel().levelStr : "")
            ))
            .sorted(Comparator.comparing(LoggerLevel::getLevel)
                .reversed()
                .thenComparing(LoggerLevel::getName))
            .collect(Collectors.toList());
    }

    public void updateLog(String name, String level, String originalLevel) {
        //Set real logger
        ((LoggerContext) LoggerFactory.getILoggerFactory()).getLogger(name).setLevel(Level.toLevel(level));
        //Get log from the log hashmap
        if (logOriginalLevelMap != null && !logOriginalLevelMap.containsKey(name)) {
            logOriginalLevelMap.put(name, originalLevel.equals("OFF") ? "" : originalLevel);
        }
    }

    public void addLog(String name, String level) {
        //Getting non-existent logger name in LoggerFactory create and add it
        Logger logger = (Logger) LoggerFactory.getLogger(name);
        logger.setLevel(Level.valueOf(level));
    }
}