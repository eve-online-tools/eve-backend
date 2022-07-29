package de.ronnywalter.eve.jobs.esi;

import de.ronnywalter.eve.jobs.SchedulableJob;
import de.ronnywalter.eve.model.EveCharacter;
import de.ronnywalter.eve.model.JobData;
import de.ronnywalter.eve.service.CharacterService;
import lombok.extern.slf4j.Slf4j;
import net.evetech.esi.client.api.CharacterApi;
import net.evetech.esi.client.api.CorporationApi;
import net.evetech.esi.client.model.GetCharactersCharacterIdOk;
import net.evetech.esi.client.model.GetCharactersCharacterIdPortraitOk;
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
public class CharacterUpdateJob extends EsiApiJob {

    @Autowired
    private CharacterService characterService;

    private CharacterApi characterApi = new CharacterApi(getApiClient());
    private CorporationApi corporationApi = new CorporationApi(getApiClient());

    public CharacterUpdateJob() {
        super(CharacterUpdateJob.class.getName());
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

        ResponseEntity<GetCharactersCharacterIdOk> result = update(new Update<ResponseEntity<GetCharactersCharacterIdOk>>() {
            @Override
            public ResponseEntity<GetCharactersCharacterIdOk> update() throws HttpClientErrorException {
                return characterApi.getCharactersCharacterIdWithHttpInfo(c.getId(), DATASOURCE, null);
            }
        });

        jobData.setNextExecutionTime(getExpiryDate(result.getHeaders()).plusSeconds(1));

        if (result != null) {
            GetCharactersCharacterIdOk x = result.getBody();
            c.setName(x.getName());
            c.setAllianceId(x.getAllianceId() != null ? x.getAllianceId() : null);
            c.setCorporationId(x.getCorporationId());
            c.setSecurityStatus(x.getSecurityStatus());

            GetCorporationsCorporationIdOk corpData = update(new Update<GetCorporationsCorporationIdOk>() {
                @Override
                public GetCorporationsCorporationIdOk update() throws HttpClientErrorException {
                    return corporationApi.getCorporationsCorporationId(c.getCorporationId(), DATASOURCE, null);
                }
            });

            if(corpData != null) {
                c.setCorporationName(corpData.getName());
                c.setCorporationTicker(corpData.getTicker());
            }

            ResponseEntity<GetCharactersCharacterIdPortraitOk> portrait = update(new Update<ResponseEntity<GetCharactersCharacterIdPortraitOk>>() {
                @Override
                public ResponseEntity<GetCharactersCharacterIdPortraitOk> update() throws HttpClientErrorException {
                    return characterApi.getCharactersCharacterIdPortraitWithHttpInfo(c.getId(), DATASOURCE, null);
                }
            });
            c.setPortrait64(portrait.getBody().getPx64x64());
            c.setPortrait128(portrait.getBody().getPx128x128());
            c.setPortrait256(portrait.getBody().getPx256x256());
            c.setPortrait512(portrait.getBody().getPx512x512());

            ResponseEntity<GetCorporationsCorporationIdIconsOk> resultIcon = update(new Update<ResponseEntity<GetCorporationsCorporationIdIconsOk>>() {
                @Override
                public ResponseEntity<GetCorporationsCorporationIdIconsOk> update() throws HttpClientErrorException {
                    return corporationApi.getCorporationsCorporationIdIconsWithHttpInfo(c.getCorporationId(), DATASOURCE, null);
                }
            });
            if(resultIcon != null) {
                c.setCorpLogo(resultIcon.getBody().getPx128x128());
            }
            characterService.saveCharacter(c);
        }
    }

    public void updateChar(int characterId) {
        JobData jobData = new JobData();
        Map<String, String> jobParams = new HashMap<>();
        jobParams.put("characterId", "" + characterId);
        jobData.setJobParams(jobParams);
        run(jobData);
    }
}


