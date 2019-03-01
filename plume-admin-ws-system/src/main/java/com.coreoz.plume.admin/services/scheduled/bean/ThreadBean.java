package com.coreoz.plume.admin.services.scheduled.bean;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ThreadBean {
    private int activeThreads;
    private int inactiveThreads;
}
