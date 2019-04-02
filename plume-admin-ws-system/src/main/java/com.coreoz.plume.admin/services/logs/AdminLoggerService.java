package com.coreoz.plume.admin.services.logs;

import ch.qos.logback.classic.Level;
import com.coreoz.plume.admin.services.logs.bean.LoggerLevel;

import org.slf4j.LoggerFactory;
import ch.qos.logback.classic.LoggerContext;

import javax.inject.Singleton;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Singleton
public class AdminLoggerService {

    private final ConcurrentHashMap<String, String> originalLoggerLevels = new ConcurrentHashMap<>();

    public List<LoggerLevel> getLoggerLevels() {
        return ((LoggerContext) LoggerFactory.getILoggerFactory()).getLoggerList()
            .stream()
            .map(logger -> new LoggerLevel(
                logger.getName(),
                logger.getLevel() != null && !logger.getLevel().equals(Level.OFF) ? logger.getLevel().levelStr : "",
                originalLoggerLevels.getOrDefault(logger.getName(), logger.getLevel() != null ? logger.getLevel().levelStr : "")
            ))
            .sorted(Comparator.comparing(LoggerLevel::getLevel)
                .reversed()
                .thenComparing(LoggerLevel::getName))
            .collect(Collectors.toList());
    }

    public void changeLoggerLevel(String name, String level) {
        Level currentLevel = ((LoggerContext) LoggerFactory.getILoggerFactory()).getLogger(name).getLevel();
        if (!originalLoggerLevels.containsKey(name)) {
            originalLoggerLevels.put(name, currentLevel == null || Level.OFF.equals(currentLevel) ? "" : currentLevel.levelStr);
        }
        ((LoggerContext) LoggerFactory.getILoggerFactory()).getLogger(name).setLevel(Level.toLevel(level));
    }
}