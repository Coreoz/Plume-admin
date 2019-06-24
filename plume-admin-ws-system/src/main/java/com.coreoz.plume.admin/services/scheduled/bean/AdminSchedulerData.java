package com.coreoz.plume.admin.services.scheduled.bean;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class AdminSchedulerData {
    private List<AdminSchedulerJob> jobs;
    private AdminSchedulerThreadStats threadStats;
}
