package de.ronnywalter.eve.jobs.esi;

import de.ronnywalter.eve.jobs.SchedulableJob;
import de.ronnywalter.eve.model.CorpWallet;
import de.ronnywalter.eve.model.Corporation;
import de.ronnywalter.eve.model.JobData;
import de.ronnywalter.eve.service.CorporationService;
import de.ronnywalter.eve.service.TokenService;
import de.ronnywalter.eve.service.WalletService;
import lombok.extern.slf4j.Slf4j;
import net.evetech.esi.client.api.WalletApi;
import net.evetech.esi.client.model.GetCorporationsCorporationIdWallets200Ok;
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
//@SchedulableJob (scheduleTime = "30 */5 * * * *")
@SchedulableJob
public class CorpWalletImportJob extends EsiApiJob {

    private final WalletApi walletApi = new WalletApi(getApiClient());
    @Autowired
    private CorporationService corporationService;
    @Autowired
    private WalletService walletService;
    @Autowired
    private TokenService tokenService;

    public CorpWalletImportJob() {
        super(CorpWalletImportJob.class.getName());
    }

    @Override
    public List<JobData> init() {
        List<JobData> jobDataList = new ArrayList<>();
        List<Corporation> corps = corporationService.getCorporations();
        corps.forEach(c -> {
            JobData jobData = initJobData("corp-wallet: " + c.getTicker());
            Map<String, String> params = new HashMap<>();
            params.put("corpId", "" + c.getId());
            jobData.setJobParams(params);
            jobDataList.add(jobData);
        });
        return jobDataList;
    }

    @Override
    public void run(JobData jobData) {
        Integer corpId = Integer.parseInt(jobData.getJobParams().get("corpId"));
        Corporation c = corporationService.getCorporation(corpId);
        ResponseEntity<List<GetCorporationsCorporationIdWallets200Ok>> result = update(new Update<ResponseEntity<List<GetCorporationsCorporationIdWallets200Ok>>>() {
            @Override
            public ResponseEntity<List<GetCorporationsCorporationIdWallets200Ok>> update() throws HttpClientErrorException {
                return walletApi.getCorporationsCorporationIdWalletsWithHttpInfo(c.getId(), DATASOURCE, null, tokenService.getApiToken(c.getCeoId()));
            }
        });
        if (result != null) {
            result.getBody().forEach(x -> {
                CorpWallet corpWallet = new CorpWallet(c.getId(), x.getDivision());
                corpWallet.setValue(x.getBalance());
                walletService.saveCorpWallet(corpWallet);
                log.info("Corp-Wallet updated: " + corpWallet);
            });

        }
        log.info("Expiry-Date: " + getExpiryDate(result.getHeaders()));
        jobData.setNextExecutionTime(getExpiryDate(result.getHeaders()).plusSeconds(60));
    }

}
