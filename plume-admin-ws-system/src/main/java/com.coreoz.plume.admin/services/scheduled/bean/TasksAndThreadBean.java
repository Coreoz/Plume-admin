package com.coreoz.plume.admin.services.scheduled.bean;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class TasksAndThreadBean {

    private List<AsyncTaskBean> asyncTaskList;
    private ThreadBean threadStats;
}
