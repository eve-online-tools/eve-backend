package de.ronnywalter.eve.service;

import com.github.kagkarlsson.scheduler.Scheduler;
import com.github.kagkarlsson.scheduler.task.schedule.Schedule;
import com.github.kagkarlsson.scheduler.task.schedule.Schedules;
import de.ronnywalter.eve.jobs.AbstractJob;
import de.ronnywalter.eve.jobs.SchedulableJob;
import de.ronnywalter.eve.jobs.core.JobScheduleAndData;
import de.ronnywalter.eve.jobs.esi.*;
import de.ronnywalter.eve.model.JobData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
public class JobSchedulerService implements ApplicationContextAware {

    private ApplicationContext applicationContext;
    @Autowired
    private JobDataService jobDataService;

    @Autowired
    private Scheduler scheduler;

    private boolean scheduleJobs = true;

    //@Autowired
    //private Scheduler scheduler;

    //@Autowired
    //private SchedulerClient schedulerClient;

    public <T extends AbstractJob> void scheduleAllJobs() {
        Map<String,Object> beans =  applicationContext.getBeansWithAnnotation(SchedulableJob.class);

        if(scheduleJobs) {
	        beans.keySet().forEach(s -> {
	            T job = (T) beans.get(s);
                //if(job instanceof WalletTransactionsImportJob) {
                    List<JobData> jobInstances = job.init();
                    jobInstances.forEach(jobData -> {
                        log.info("Scheduling job " + jobData.getJobName() + " for " + jobData.getNextExecutionTime());
                        if (jobData.getLastExecutionTime() == null) {
                            jobData.setNextExecutionTime(Instant.now());
                            scheduleJob(job, jobData);
                        } else if (jobData.isCronJob()) {
                            CronExpression ce = CronExpression.parse(jobData.getCronExpression());
                            LocalDateTime next = ce.next(LocalDateTime.now());
                            jobData.setNextExecutionTime(next.toInstant(ZoneOffset.UTC));
                            scheduleJob(job, jobData);
                        } else {
                            scheduleJob(job, jobData);
                        }
                        jobDataService.saveJobData(jobData);
                    });
                //}
	        });

        }

    }

    private <T extends AbstractJob> void scheduleJob(AbstractJob job, JobData jd) {
        JobScheduleAndData jobScheduleAndData = new JobScheduleAndData();
        jobScheduleAndData.setJobDataId(jd.getId());
        jobScheduleAndData.setNextExecutionTime(jd.getNextExecutionTime());


        scheduler.schedule(job.schedulableInstance(Long.valueOf(jd.getId()).toString(), jobScheduleAndData));
    }

    /*
    private <T extends AbstractJob> UUID scheduleJob(Instant time, JobData jd) {
        JobRequest jr = new JobRequest();
        jr.setJobClass(jd.getJobClassName());
        jr.setJobDataId(jd.getId());
        jr.setName(jd.getJobName());
        return jobRequestScheduler.schedule(time, jr).asUUID();
    }

    private <T extends AbstractJob> String scheduleCronJob(JobData jd) {
        JobRequest jr = new JobRequest();
        jr.setJobClass(jd.getJobClassName());
        jr.setJobDataId(jd.getId());
        jr.setName(jd.getJobName());
        return jobRequestScheduler.scheduleRecurrently(UUID.randomUUID().toString(), jd.getCronExpression(), jr);
    }


     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    public void startJobNow(Long jobId) {
    /*    JobData jobData = jobDataService.getJobData(jobId);
        try {
            Class jobClass = Class.forName(jobData.getJobClassName());
            Map<String, AbstractJob> beans =  applicationContext.getBeansOfType(jobClass);
            beans.values().forEach(job -> {
                ScheduledExecution<Object> scheduledExecution = scheduler.getScheduledExecution(job.instance(jobData.getNextTaskInstance())).orElse(null);
                if(scheduledExecution != null) {
                    scheduler.reschedule(job.instance(jobData.getNextTaskInstance()), Instant.now());
                } else {
                    scheduler.schedule(job.instance(jobData.getNextTaskInstance(), jobData.getId()), Instant.now());
                }
            });
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

     */
    }


}
