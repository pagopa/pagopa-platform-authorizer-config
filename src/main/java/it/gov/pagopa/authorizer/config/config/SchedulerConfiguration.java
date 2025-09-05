package it.gov.pagopa.authorizer.config.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
@ConditionalOnProperty(name = "authorizer.schedule.enabled", matchIfMissing = false)
public class SchedulerConfiguration {}