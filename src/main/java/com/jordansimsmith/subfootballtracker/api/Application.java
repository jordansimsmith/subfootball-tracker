package com.jordansimsmith.subfootballtracker.api;

import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.leader.LeaderSelector;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@ConfigurationPropertiesScan
@SpringBootApplication
public class Application implements CommandLineRunner {

    private final RegistrationTracker registrationTracker;
    private final String zkHost;

    @Autowired
    public Application(RegistrationTracker registrationTracker, @Value("${zkhost}") String zkHost) {
        this.registrationTracker = registrationTracker;
        this.zkHost = zkHost;
    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        var curator =
                CuratorFrameworkFactory.newClient(zkHost, new ExponentialBackoffRetry(1000, 3));
        curator.start();

        var selector =
                new LeaderSelector(curator, "/registration-tracker-leader", registrationTracker);
        selector.autoRequeue();
        selector.start();
    }
}
