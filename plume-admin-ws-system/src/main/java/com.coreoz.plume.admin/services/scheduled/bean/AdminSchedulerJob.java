package com.coreoz.plume.admin.services.scheduled.bean;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class AdminSchedulerJob {
    private String name;
    private String frequency;
    private long executionsCount;
    private long nextExecutionTimeInMillis;
    private long lastExecutionStartedTimeInMillis;
    private long lastExecutionEndedTimeInMillis;
    private String status;
    private boolean canBeRun;
}
