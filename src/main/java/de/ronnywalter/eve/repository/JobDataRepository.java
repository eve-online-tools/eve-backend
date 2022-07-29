package de.ronnywalter.eve.repository;

import de.ronnywalter.eve.model.JobData;
import org.springframework.data.repository.CrudRepository;

import java.util.List;


public interface JobDataRepository extends CrudRepository<JobData, Long> {
    JobData findByJobClassNameAndJobName(String jobClassName, String jobName);
    List<JobData> findByJobClassName(String jobClassName);
}
