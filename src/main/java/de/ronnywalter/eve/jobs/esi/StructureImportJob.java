package de.ronnywalter.eve.jobs.esi;

import com.google.common.collect.Lists;
import de.ronnywalter.eve.jobs.SchedulableJob;
import de.ronnywalter.eve.model.EveCharacter;
import de.ronnywalter.eve.model.JobData;
import de.ronnywalter.eve.model.Location;
import de.ronnywalter.eve.model.LocationType;
import de.ronnywalter.eve.service.CharacterService;
import de.ronnywalter.eve.service.TokenService;
import de.ronnywalter.eve.service.TypeService;
import de.ronnywalter.eve.service.UniverseService;
import lombok.extern.slf4j.Slf4j;
import net.evetech.esi.client.api.MarketApi;
import net.evetech.esi.client.api.UniverseApi;
import net.evetech.esi.client.model.GetMarketsStructuresStructureId200Ok;
import net.evetech.esi.client.model.GetUniverseStructuresStructureIdOk;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


@Service
@Slf4j
@SchedulableJob(scheduleTime = "0 15 11 * * *")
public class StructureImportJob extends EsiApiJob {

    private final UniverseApi universeApi = new UniverseApi(getApiClient());
    private final MarketApi marketApi = new MarketApi(getApiClient());

    @Autowired
    private UniverseService universeService;
    @Autowired
    private TypeService typeService;
    @Autowired
    private TokenService tokenService;
    @Autowired
    private CharacterService characterService;

    @Value("${structures.file}")
    private String structuresFile;

    public StructureImportJob() {
        super(StructureImportJob.class.getName());
    }

    @Transactional
    public Location updateStructure(long id, int characterId) {
        log.info("getting structure " + id);

        ResponseEntity<GetUniverseStructuresStructureIdOk> struc = update(new Update<ResponseEntity<GetUniverseStructuresStructureIdOk>>() {
              @Override
              public ResponseEntity<GetUniverseStructuresStructureIdOk> update() throws HttpClientErrorException {
                  return universeApi.getUniverseStructuresStructureIdWithHttpInfo(id, DATASOURCE, null, tokenService.getApiToken(characterId));
              }
          }, false
        );

        ResponseEntity<List<GetMarketsStructuresStructureId200Ok>> strucMarket = update(new Update<ResponseEntity<List<GetMarketsStructuresStructureId200Ok>>>() {
                  @Override
                  public ResponseEntity<List<GetMarketsStructuresStructureId200Ok>> update() throws HttpClientErrorException {
                      return marketApi.getMarketsStructuresStructureIdWithHttpInfo(id, DATASOURCE, null, 1, tokenService.getApiToken(characterId));
                  }
              }, false
        );

        Location structure = universeService.getStructure(id);
        if(structure == null) {
            structure = new Location();
            structure.setId(id);
            structure.setLocationType(LocationType.STRUCTURE);
        }

        if (struc != null) {
            if(struc.getStatusCodeValue() == 403) {
                log.info("forbidden!");
                structure.setAccessForbidden(true);
                structure.setName("unknown");
                universeService.saveLocation(structure);
                return structure;
            }
            if (struc.getBody() != null) {
                GetUniverseStructuresStructureIdOk s = struc.getBody();
                structure.setSolarsystemId(struc.getBody().getSolarSystemId());
                structure.setName(struc.getBody().getName());
                structure.setAccessForbidden(false);
                structure.setHasMarket(true);
                structure.setOwnerCorpId(struc.getBody().getOwnerId());
                structure.setTypeId(struc.getBody().getTypeId());
                if (strucMarket.getStatusCodeValue() == 403) {
                    log.info("structure " + structure.getId() + " has no market access.");
                    structure.setHasMarket(false);
                }
                universeService.saveLocation(structure);
                return structure;
            }
        }
        return null;
    }

    @Transactional
    public void importStructuresFromFile() {
        List<Location> forbiddenStructures = universeService.getForbiddenStructures();
        List<Location> knownStructures = universeService.getStructures();
        Set<Long> knownIds = new HashSet<>();
        knownStructures.forEach(s -> {
            knownIds.add(s.getId());
        });

        if(structuresFile != null) {
            List<String> ids = new ArrayList<>();
            ClassPathResource resource = new ClassPathResource(structuresFile);
            try ( BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream()))) {
                reader.lines().forEach(line -> {
                    ids.add(line);
                });
            } catch (IOException e) {
                log.error(e.getMessage());
            }
            log.info("got " + ids.size() + " structure-ids from file");

            List<EveCharacter> chars = characterService.getEveCharacters();
            int count = 0;
            if(chars.size() > 0) {
                for(String structureId : ids) {
                    count++;
                    log.info("processing structure " + count + "/" + ids.size());
                    Long sId = Long.parseLong(structureId);
                    if(!knownIds.contains(sId) && !forbiddenStructures.contains(sId)) {
                        Location s = updateStructure(sId, chars.get(0).getId());
                        if(s == null || s.getAccessForbidden()) {
                            try {
                                Thread.sleep(500);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            } else {
                log.error("To import structure data, we need at least one registered char!");
            }


        }
    }

    @Override
    public List<JobData> init() {
        JobData jobData = initJobData("structures");
        return Lists.newArrayList(jobData);
    }

    @Override
    public void run(JobData jobData) {

        importStructuresFromFile();

        ResponseEntity<Set<Long>> response = update(new Update<ResponseEntity<Set<Long>>>() {
            @Override
            public ResponseEntity<Set<Long>> update() throws HttpClientErrorException {
                return universeApi.getUniverseStructuresWithHttpInfo(DATASOURCE, "market", null);
            }
        });

        log.info("Found " + response.getBody().size() + " structure ids.");

        Set<Long> structureIds = new HashSet<>();
        structureIds.addAll(response.getBody());
        structureIds.addAll(universeService.getStructureIds());


        // TODO: move to configfile
        EveCharacter eveCharacter = characterService.getEveCharacter(1276540478);
        if(eveCharacter != null) {
            structureIds.forEach(r -> {
                updateStructure(r, eveCharacter.getId());
            });
        }
        //} else {
        //    log.error("To import structure data, we need at least one registered char!");
        //}
    }
}
