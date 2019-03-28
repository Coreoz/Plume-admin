package com.coreoz.plume.admin.services.scheduled.bean;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ThreadBean {
    private int activeThreads;
    private int inactiveThreads;
    private int minThreads;
    private int maxThreads;
    private int largestPoolSize;
}
