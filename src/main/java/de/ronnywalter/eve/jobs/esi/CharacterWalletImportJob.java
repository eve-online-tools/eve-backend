package de.ronnywalter.eve.jobs.esi;

import de.ronnywalter.eve.jobs.SchedulableJob;
import de.ronnywalter.eve.model.CharacterWallet;
import de.ronnywalter.eve.model.EveCharacter;
import de.ronnywalter.eve.model.JobData;
import de.ronnywalter.eve.service.CharacterService;
import de.ronnywalter.eve.service.TokenService;
import de.ronnywalter.eve.service.WalletService;
import lombok.extern.slf4j.Slf4j;
import net.evetech.esi.client.api.WalletApi;
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
//@SchedulableJob (scheduleTime = "0 */5 * * * *")
@SchedulableJob
public class CharacterWalletImportJob extends EsiApiJob {

    @Autowired
    private CharacterService characterService;
    @Autowired
    private WalletService walletService;
    @Autowired
    private TokenService tokenService;

    private final WalletApi walletApi = new WalletApi(getApiClient());

    public CharacterWalletImportJob() {
        super(CharacterWalletImportJob.class.getName());
    }

    @Override
    public List<JobData> init() {
        List<JobData> jobDataList = new ArrayList<>();
        List<EveCharacter> characters = characterService.getEveCharacters();
        characters.forEach(c -> {
            JobData jobData = initJobData(c.getName());
            Map<String, String> params = new HashMap<>();
            params.put("characterId", "" + c.getId());
            jobData.setJobParams(params);
            jobDataList.add(jobData);
        });
        return jobDataList;
    }


    @Override
    public void run(JobData jobData) {
        int characterId = Integer.parseInt(jobData.getJobParams().get("characterId"));
        EveCharacter c = characterService.getEveCharacter(characterId);
        log.info("Updating wallet for " + c.getName());
        ResponseEntity<Double> result = update(new Update<ResponseEntity<Double>>() {
            @Override
            public ResponseEntity<Double> update() throws HttpClientErrorException {
                return walletApi.getCharactersCharacterIdWalletWithHttpInfo(c.getId(), DATASOURCE, null, tokenService.getApiToken(c.getId()));
            }
        });
        if (result != null) {
            CharacterWallet w = walletService.getCharacterWallet(c.getId());
            if(w == null) {
                w = new CharacterWallet();
                w.setId(c.getId());
                w.setCharacterId(c.getId());
            }
            w.setValue(result.getBody());
            log.info("Wallet of character: " + c.getName() + ": " + w.getValue());
            walletService.saveCharacterWallet(w);
        }
        log.info("Expiry-Date: " + getExpiryDate(result.getHeaders()));
        jobData.setNextExecutionTime(getExpiryDate(result.getHeaders()).plusSeconds(60));
    }
}
