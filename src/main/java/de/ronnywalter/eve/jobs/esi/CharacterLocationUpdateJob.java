package de.ronnywalter.eve.jobs.esi;

import com.github.kagkarlsson.scheduler.task.CompletionHandler;
import com.github.kagkarlsson.scheduler.task.ExecutionContext;
import com.github.kagkarlsson.scheduler.task.TaskInstance;
import de.ronnywalter.eve.jobs.JobInterface;
import de.ronnywalter.eve.jobs.SchedulableJob;
import de.ronnywalter.eve.jobs.core.JobScheduleAndData;
import de.ronnywalter.eve.model.EveCharacter;
import de.ronnywalter.eve.model.JobData;
import de.ronnywalter.eve.service.CharacterService;
import de.ronnywalter.eve.service.TokenService;
import de.ronnywalter.eve.service.UniverseService;
import lombok.extern.slf4j.Slf4j;
import net.evetech.esi.client.api.LocationApi;
import net.evetech.esi.client.model.GetCharactersCharacterIdLocationOk;
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
@SchedulableJob(scheduleTime = "0 * * * * *")
public class CharacterLocationUpdateJob extends EsiApiJob implements JobInterface {

    @Autowired
    private CharacterService characterService;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private UniverseService universeService;

    private LocationApi locationApi = new LocationApi(getApiClient());

    public CharacterLocationUpdateJob() {
        super(CharacterLocationUpdateJob.class.getName());
    }

    @Override
    public List<JobData> init() {
        List<JobData> jobDataList = new ArrayList<>();
        List<EveCharacter> characters = characterService.getEveCharacters();
        characters.forEach(c -> {
            Map<String, String> params = new HashMap<>();
            params.put("characterId", "" + c.getId());

            JobData jobData = initJobData(c.getName());

            jobData.setJobParams(params);
            jobDataList.add(jobData);
        });
        return jobDataList;
    }


    @Override
    public void run(JobData jobData) {
        int characterId = Integer.parseInt(jobData.getJobParams().get("characterId"));
        EveCharacter c = characterService.getEveCharacter(characterId);

        ResponseEntity<GetCharactersCharacterIdLocationOk> result = update(new Update<ResponseEntity<GetCharactersCharacterIdLocationOk>>() {
            @Override
            public ResponseEntity<GetCharactersCharacterIdLocationOk> update() throws HttpClientErrorException {
                return locationApi.getCharactersCharacterIdLocationWithHttpInfo(c.getId(), DATASOURCE, null, tokenService.getApiToken(characterId));
            }
        });
        if (result != null && result.getBody() != null) {
            GetCharactersCharacterIdLocationOk x = result.getBody();

            Integer stationId = x.getStationId();
            Long structureId = x.getStructureId();
            Long locationId = null;
            if(structureId != null) {
                locationId = structureId;
            } else if(stationId != null) {
                locationId = new Long(stationId);
            }
            if(locationId != null) {
                c.setLocationId(locationId);
            } else {
                c.setLocationId(null);
            }
            c.setSolarSystemId(x.getSolarSystemId());
            characterService.saveCharacter(c);
            jobData.setNextExecutionTime(getExpiryDate(result.getHeaders()).plusSeconds(60));
        }

    }

}


