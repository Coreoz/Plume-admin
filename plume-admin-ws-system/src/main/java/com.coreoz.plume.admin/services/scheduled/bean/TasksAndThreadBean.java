package com.coreoz.plume.admin.services.scheduled.bean;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class TasksAndThreadBean {
    private List<AsyncTask> asyncTasks;
    private ThreadBean threadStats;
}
