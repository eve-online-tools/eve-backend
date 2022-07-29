package de.ronnywalter.eve.jobs.esi;

import de.ronnywalter.eve.jobs.SchedulableJob;
import de.ronnywalter.eve.model.*;
import de.ronnywalter.eve.service.TypeService;
import de.ronnywalter.eve.service.UniverseService;
import lombok.extern.slf4j.Slf4j;
import net.evetech.esi.client.api.UniverseApi;
import net.evetech.esi.client.model.GetUniverseConstellationsConstellationIdOk;
import net.evetech.esi.client.model.GetUniverseRegionsRegionIdOk;
import net.evetech.esi.client.model.GetUniverseStationsStationIdOk;
import net.evetech.esi.client.model.GetUniverseSystemsSystemIdOk;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;


@Service
@Slf4j
@SchedulableJob(scheduleTime = "0 30 11 * * *")
public class UniverseImportJob extends EsiApiJob {

    @Autowired
    private UniverseService universeService;
    @Autowired
    private TypeService typeService;

    private final Executor executor = Executors.newFixedThreadPool(10);

    private UniverseApi universeApi = new UniverseApi(getApiClient());

    public UniverseImportJob() {
        super(UniverseImportJob.class.getName());
    }

    private void updateConstellation(int constellationId, Region region) {
        GetUniverseConstellationsConstellationIdOk response = update(new Update<GetUniverseConstellationsConstellationIdOk>() {
            @Override
            public GetUniverseConstellationsConstellationIdOk update() throws HttpClientErrorException {
                return universeApi.getUniverseConstellationsConstellationId(constellationId, LANGUAGE, DATASOURCE, null, LANGUAGE);
            }
        });

        Constellation constellation = universeService.getConstellation(constellationId);
        if(constellation == null) {
            constellation = new Constellation();
            constellation.setId(constellationId);
        }
        constellation.setName(response.getName());
        constellation.setRegionId(region.getId());
        universeService.saveConstellation(constellation);
        for(Integer s: response.getSystems()) {
            updateSystem(s, constellation);
        }
        log.info("Updated constellation: " + constellation.getName());

    }

    private void updateSystem(int systemId, Constellation constellation) {
        GetUniverseSystemsSystemIdOk response = update(new Update<GetUniverseSystemsSystemIdOk>() {
            @Override
            public GetUniverseSystemsSystemIdOk update() throws HttpClientErrorException {
                return universeApi.getUniverseSystemsSystemId(systemId, LANGUAGE, DATASOURCE, null, LANGUAGE);
            }
        });

        SolarSystem system = universeService.getSolarSystem(systemId);
        if(system == null) {
            system = new SolarSystem();
            system.setId(systemId);
        }
        system.setConstellationId(constellation.getId());
        system.setRegionId(constellation.getRegionId());
        system.setName(response.getName());
        system.setSecurityStatus(response.getSecurityStatus());

        universeService.saveSolarSystem(system);

        if(response.getStations() != null) {
            for(Integer s : response.getStations()) {
                updateStation(s, system);
            };
        }
        log.info("Updated solarsystem: " + system.getName());

    }

    private void updateStation(Integer stationId, SolarSystem system) {
        GetUniverseStationsStationIdOk response = update(new Update<GetUniverseStationsStationIdOk>() {
            @Override
            public GetUniverseStationsStationIdOk update() throws HttpClientErrorException {
                return universeApi.getUniverseStationsStationId(stationId, DATASOURCE, null);
            }
        });
        Location s = universeService.getLocation(stationId);
        if(s == null) {
            s = new Location();
            s.setId(Long.valueOf(stationId));
            s.setLocationType(LocationType.STATION);
        }
        s.setSolarsystemId(system.getId());
        s.setConstellationId(system.getConstellationId());
        s.setRegionId(system.getRegionId());
        s.setName(response.getName());
        s.setOwnerCorpId(response.getOwner());
        s.setTypeId(response.getTypeId());
        log.info("Updated station: " + s.getName());
        universeService.saveLocation(s);
    }

    @Override
    public List<JobData> init() {
        List<JobData> jobDataList = new ArrayList<>();
        List<Integer> regionsToProcess = new ArrayList<>();

        ResponseEntity<List<Integer>> ids = update(new Update<ResponseEntity<List<Integer>>>() {
            @Override
            public ResponseEntity<List<Integer>> update() throws HttpClientErrorException {
                return universeApi.getUniverseRegionsWithHttpInfo(DATASOURCE, null);
            }
        });
        regionsToProcess.addAll(ids.getBody());
        regionsToProcess.forEach(r -> {
            JobData jobData = initJobData("Universe-update, region: " + String.valueOf(r));
            Map<String, String> params = new HashMap<>();
            params.put("regionId", "" + r);
            jobData.setJobParams(params);
            jobDataList.add(jobData);
        });
        return jobDataList;
    }

    @Override
    public void run(JobData jobData) {
        int regionId = Integer.parseInt(jobData.getJobParams().get("regionId"));
        log.info("Updating region: " + regionId);

        GetUniverseRegionsRegionIdOk response = update(new Update<GetUniverseRegionsRegionIdOk>() {
            @Override
            public GetUniverseRegionsRegionIdOk update() throws HttpClientErrorException {
                return universeApi.getUniverseRegionsRegionId(regionId, LANGUAGE,DATASOURCE, null, LANGUAGE);
            }
        });

        Region region = universeService.getRegion(regionId);
        if(region == null) {
            region = new Region();
            region.setId(response.getRegionId());
        }
        region.setName(response.getName());
        region.setDescription(response.getDescription());
        universeService.saveRegion(region);

        for(Integer constellationId : response.getConstellations()) {
            updateConstellation(constellationId, region);
        }

        log.info("Updated region: " + region.getName());
        universeService.saveRegion(region);
    }

}
