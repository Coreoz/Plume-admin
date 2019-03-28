package com.coreoz.plume.admin.services.scheduled.bean;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class AsyncTask {
    private String name;
    private String frequency;
    private long nbExecution;
    private long nextExecution;
    private long previousExecutionStart;
    private long previousExecutionEnd;
    private String status;
    private boolean canBeRun;
}
