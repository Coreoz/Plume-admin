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
    private Long nextExecutionTimeInMillis;
    private Long lastExecutionStartedTimeInMillis;
    private Long lastExecutionEndedTimeInMillis;
    private String status;
    private boolean canBeRun;
}
