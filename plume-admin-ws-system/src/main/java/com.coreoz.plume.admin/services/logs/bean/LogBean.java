package com.coreoz.plume.admin.services.logs.bean;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LogBean {
    private int id;
    private String name;
    private String level;
    private String oldLevel;
}
