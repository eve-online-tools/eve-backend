package de.ronnywalter.eve.service;

import com.google.common.collect.Lists;
import de.ronnywalter.eve.model.JobData;
import de.ronnywalter.eve.repository.JobDataRepository;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
@Getter
public class JobDataService {

    private final JobDataRepository jobDataRepository;

    private List<JobData> getAllJobData() {
        return Lists.newArrayList(jobDataRepository.findAll());
    }

    public List<JobData> getJobData(String jobClassName) {
        return jobDataRepository.findByJobClassName(jobClassName);
    }

    public JobData getJobData(String jobClassName, String jobName) {
        return jobDataRepository.findByJobClassNameAndJobName(jobClassName, jobName);
    }

    public JobData getJobData(long id) {
        return jobDataRepository.findById(id).orElse(null);
    }

    public JobData saveJobData(JobData data) {
        log.debug("Storing metadata: " + data);
        return jobDataRepository.save(data);
    }


}
