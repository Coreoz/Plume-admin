package com.coreoz.plume.admin.services.logs.bean;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class LoggerLevel {
    private String name;
    private String level;
    private String originalLevel;
}
