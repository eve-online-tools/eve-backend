package de.ronnywalter.eve.jobs;

import de.ronnywalter.eve.service.JobSchedulerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class ApplicationStartupJobScheduler implements ApplicationListener<ApplicationReadyEvent>, ApplicationContextAware {

    private final JobSchedulerService jobSchedulerService;
    private ApplicationContext applicationContext;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        log.info("Application has started.");
        jobSchedulerService.scheduleAllJobs();
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
