package com.coreoz.plume.admin.services.logs;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import javax.inject.Singleton;

import org.slf4j.LoggerFactory;

import com.coreoz.plume.admin.services.logs.bean.LoggerLevel;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;

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
        Logger logger = ((LoggerContext) LoggerFactory.getILoggerFactory()).getLogger(name);
        if (!originalLoggerLevels.containsKey(name)) {
            originalLoggerLevels.put(name, logger.getLevel() == null || Level.OFF.equals(logger.getLevel()) ? "" : logger.getLevel().levelStr);
        }
        logger.setLevel(Level.toLevel(level));
    }
}
