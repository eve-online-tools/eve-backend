package de.ronnywalter.eve.controller;

import de.ronnywalter.eve.service.JobDataService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/jobs")
public class JobController {

	private final JobDataService jobDataService;
	//private final JobSchedulerService jobSchedulerService;

	@RequestMapping(value = "/run/{jobId}", method = { RequestMethod.POST })
	public Object runJob(@PathVariable Long jobId) {
		//jobSchedulerService.startJobNow(jobId);
		return "OK";
	}

	@RequestMapping(value = "/runAll/{jobClassName}", method = { RequestMethod.POST })
	public Object runJob(@PathVariable String jobClassName) {
		//jobDataService.startJobsNow(jobClassName);
		return "OK";
	}
}
