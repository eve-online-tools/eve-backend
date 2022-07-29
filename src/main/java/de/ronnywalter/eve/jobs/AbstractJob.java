package de.ronnywalter.eve.jobs;


import com.github.kagkarlsson.scheduler.task.*;
import com.github.kagkarlsson.scheduler.task.helper.RecurringTaskWithPersistentSchedule;
import de.ronnywalter.eve.jobs.core.JobScheduleAndData;
import de.ronnywalter.eve.model.JobData;
import de.ronnywalter.eve.service.JobDataService;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.scheduling.support.CronExpression;

import java.lang.annotation.Annotation;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Slf4j
@Getter
@Setter
public abstract class AbstractJob extends RecurringTaskWithPersistentSchedule<JobScheduleAndData> {

    @Autowired
    protected JobDataService jobDataService;

    public AbstractJob(String name) {
        super(name, JobScheduleAndData.class);
    }


    public abstract List<JobData> init();
    public abstract void run(JobData jobData);



    @Override
    public CompletionHandler<JobScheduleAndData> execute(TaskInstance<JobScheduleAndData> taskInstance, ExecutionContext executionContext) {
        long now = System.currentTimeMillis();
        JobScheduleAndData jobScheduleAndData = taskInstance.getData();
        Long jobDataId = jobScheduleAndData.getJobDataId();

        JobData jobData = jobDataService.getJobData(jobDataId);
        if(jobData.getEnabled() != null && jobData.getEnabled()) {
            log.info("Executing job: " + jobData.getJobClassName() + ": " + jobData.getJobName());
            jobData.setLastExecutionTime(Instant.now());
            run(jobData);
            jobData.incrementRunCount();
        } else {
            log.info("Job: " + jobData.getJobClassName() + ": " + jobData.getJobName() + " skipped, because it is disabled.");
        }

        if(jobData.isCronJob()) {
            CronExpression ce = CronExpression.parse(jobData.getCronExpression());
            LocalDateTime next = ce.next(LocalDateTime.now());
            jobData.setNextExecutionTime(next.toInstant(ZoneOffset.UTC));
        }

        jobScheduleAndData.setNextExecutionTime(jobData.getNextExecutionTime());
        jobData.setLastDuration(System.currentTimeMillis() - now);
        jobDataService.saveJobData(jobData);

        return (executionComplete, executionOperations) -> {
            if(jobScheduleAndData.getNextExecutionTime() != null && jobData.getEnabled() != null && jobData.getEnabled()) {
                log.info("Rescheduling job " + jobData.toString());
                executionOperations.reschedule(
                        executionComplete,
                        taskInstance.getData().getSchedule().getNextExecutionTime(executionComplete),
                        taskInstance.getData()
                );
            } else {
                log.info("Stopping job " + jobData.toString());
                executionOperations.stop();
            }
        };
    }





    public void executeOnce(Long id) {
        long now = System.currentTimeMillis();

        JobData jobData = jobDataService.getJobData(id);
        log.info("Executing job: " + jobData.getJobClassName() + ": " + jobData.getJobName());
        jobData.setLastExecutionTime(Instant.now());
        run(jobData);
        jobData.incrementRunCount();
        if(!jobData.isCronJob()) {
            if(jobData.getNextExecutionTime() != null) {
                //executionContext.getSchedulerClient().schedule(this.instance(jobData.getNextTaskInstance(), jobData.getId()), jobData.getNextExecutionTime());
            } else {
                log.warn("No Execution-Time set, skipping rescheduling of job " + jobData.getJobName());
            }
        } else {
            CronExpression ce = CronExpression.parse(jobData.getCronExpression());
            LocalDateTime next = ce.next(LocalDateTime.now());
            jobData.setNextExecutionTime(next.toInstant(ZoneOffset.UTC));
            //executionContext.getSchedulerClient().schedule(this.instance(jobData.getNextTaskInstance(), jobData.getId()), jobData.getNextExecutionTime());
        }
        jobData.setLastDuration(System.currentTimeMillis() - now);
        jobDataService.saveJobData(jobData);
    }

    protected JobData initJobData(String name) {
        JobData jobData = jobDataService.getJobData(this.getClass().getSimpleName(), name);
        if(jobData == null) {
            jobData = new JobData();
            jobData.setJobClassName(this.getClass().getSimpleName());
            jobData.setJobName(name);
            jobData.setNextExecutionTime(Instant.now());
            jobData.setEnabled(true);
        }

        Annotation annotation = AnnotationUtils.findAnnotation(this.getClass(), SchedulableJob.class);

        if(annotation != null) {
            String scheduleTimeExpression = AnnotationUtils.getValue(annotation, "scheduleTime").toString();
            if (scheduleTimeExpression.length() > 0) {
                CronExpression ce = CronExpression.parse(scheduleTimeExpression);
                LocalDateTime next = ce.next(LocalDateTime.now());
                if(jobData.getLastExecutionTime() == null) {
                    jobData.setNextExecutionTime(Instant.now());
                } else {
                    jobData.setNextExecutionTime(next.toInstant(ZoneOffset.UTC));
                }
                jobData.setCronJob(true);
                jobData.setCronExpression(scheduleTimeExpression);
            }
        }
        return jobDataService.saveJobData(jobData);
    }
}
