package de.ronnywalter.eve.jobs.esi;

import de.ronnywalter.eve.jobs.SchedulableJob;
import de.ronnywalter.eve.model.Corporation;
import de.ronnywalter.eve.model.JobData;
import de.ronnywalter.eve.service.CharacterService;
import de.ronnywalter.eve.service.CorporationService;
import lombok.extern.slf4j.Slf4j;
import net.evetech.esi.client.api.CorporationApi;
import net.evetech.esi.client.model.GetCorporationsCorporationIdIconsOk;
import net.evetech.esi.client.model.GetCorporationsCorporationIdOk;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@SchedulableJob
public class CorpUpdateJob extends EsiApiJob {

    @Autowired
    private CorporationService corporationService;
    @Autowired
    private CharacterService characterService;

    private final CorporationApi corporationApi = new CorporationApi(getApiClient());

    public CorpUpdateJob() {
        super(CorpUpdateJob.class.getName());
    }

    @Override
    public List<JobData> init() {
        List<JobData> jobDataList = new ArrayList<>();
        List<Corporation> corps = corporationService.getCorporations();
        corps.forEach(c -> {
            JobData jobData = initJobData("corp-update" + c.getTicker());
            Map<String, String> params = new HashMap<>();
            params.put("corpId", "" + c.getId());
            jobData.setJobParams(params);
            jobDataList.add(jobData);
        });
        return jobDataList;
    }


    @Override
    public void run(JobData jobData) {
        int corpId = Integer.parseInt(jobData.getJobParams().get("corpId"));
        Corporation c = corporationService.getCorporation(corpId);

        ResponseEntity<GetCorporationsCorporationIdOk> result = update(new Update<ResponseEntity<GetCorporationsCorporationIdOk>>() {
            @Override
            public ResponseEntity<GetCorporationsCorporationIdOk> update() throws HttpClientErrorException {
                return corporationApi.getCorporationsCorporationIdWithHttpInfo(c.getId(), DATASOURCE, null);
            }
        });
        if (result != null) {
            c.setName(result.getBody().getName());
            c.setTicker(result.getBody().getTicker());
            c.setCeoId(result.getBody().getCeoId());
            c.setMemberCount(result.getBody().getMemberCount());
        }

        log.info("Expiry-Date: " + getExpiryDate(result.getHeaders()));
        jobData.setNextExecutionTime(getExpiryDate(result.getHeaders()).plusSeconds(60));

        ResponseEntity<GetCorporationsCorporationIdIconsOk> resultIcon = update(new Update<ResponseEntity<GetCorporationsCorporationIdIconsOk>>() {
            @Override
            public ResponseEntity<GetCorporationsCorporationIdIconsOk> update() throws HttpClientErrorException {
                return corporationApi.getCorporationsCorporationIdIconsWithHttpInfo(c.getId(), DATASOURCE, null);
            }
        });
        if(resultIcon != null) {
            c.setLogo(resultIcon.getBody().getPx128x128());
        }
        corporationService.saveCorp(c);
        log.info("corp " + c.getName() + " updated.");
    }

    public void updateCorp(int corpId) {
        JobData jobData = new JobData();
        Map<String, String> jobParams = new HashMap<>();
        jobParams.put("corpId", "" + corpId);
        jobData.setJobParams(jobParams);
        run(jobData);
    }
}


