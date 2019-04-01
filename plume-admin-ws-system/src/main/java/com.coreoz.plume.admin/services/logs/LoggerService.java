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

    private final Map<String, String> logOriginalLevelMap = new HashMap<>();

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

    public void updateLog(String name, String level) {
        Level originalLevel = ((LoggerContext) LoggerFactory.getILoggerFactory()).getLogger(name).getLevel();
        if (!logOriginalLevelMap.containsKey(name)) {
            if (originalLevel == null) {
                logOriginalLevelMap.put(name, "");
            } else {
                logOriginalLevelMap.put(name, Level.OFF.equals(originalLevel) ? "" : originalLevel.levelStr);
            }
        }
        ((LoggerContext) LoggerFactory.getILoggerFactory()).getLogger(name).setLevel(Level.toLevel(level));
    }
}