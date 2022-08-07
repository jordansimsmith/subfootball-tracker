package com.jordansimsmith.subfootballtracker.api;

import com.jordansimsmith.subfootballtracker.api.content.ContentService;
import org.jobrunr.scheduling.JobScheduler;
import org.jobrunr.scheduling.cron.Cron;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.annotation.PostConstruct;

@SpringBootApplication
public class ApiApplication {

    private final JobScheduler jobScheduler;

    @Autowired
    public ApiApplication(JobScheduler jobScheduler) {
        this.jobScheduler = jobScheduler;
    }

    public static void main(String[] args) {
        SpringApplication.run(ApiApplication.class, args);
    }

    @PostConstruct
    public void scheduleRecurrently() {
        jobScheduler.scheduleRecurrently(Cron.daily(), ContentService::checkForUpdates);
    }
}
