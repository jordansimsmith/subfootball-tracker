package com.jordansimsmith.subfootballtracker.api;

import com.jordansimsmith.subfootballtracker.api.content.ContentService;
import org.jobrunr.configuration.JobRunr;
import org.jobrunr.scheduling.JobScheduler;
import org.jobrunr.scheduling.cron.Cron;
import org.jobrunr.storage.InMemoryStorageProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class ApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiApplication.class, args);
    }

    @Bean()
    public JobScheduler scheduleRecurrently(ApplicationContext ctx) {
        var scheduler =
                JobRunr.configure()
                        .useJobActivator(ctx::getBean)
                        .useStorageProvider(new InMemoryStorageProvider())
                        .useBackgroundJobServer()
                        .initialize()
                        .getJobScheduler();
        scheduler.scheduleRecurrently(Cron.daily(), ContentService::checkForUpdates);
        return scheduler;
    }
}
