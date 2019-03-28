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
    private long previousExecution;
    private String status;
    private boolean canBeRun;
}
