package de.ronnywalter.eve.jobs;

import de.ronnywalter.eve.model.JobData;

import java.util.List;

public interface JobInterface {

    void executeOnce(Long id);
    List<JobData> init();


}
