package com.coreoz.plume.admin.services.scheduled.bean;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AsyncTaskBean {
    private String name;
    private String frequency;
    private long nbExecution;
    private String nextExecution;
    private String previousExecution;
    private String status;
    private boolean canBeRun;
}
