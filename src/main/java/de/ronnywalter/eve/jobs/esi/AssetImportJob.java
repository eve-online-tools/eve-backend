package de.ronnywalter.eve.jobs.esi;

import com.google.common.collect.Lists;
import de.ronnywalter.eve.exception.EveCharacterNotFoundException;
import de.ronnywalter.eve.jobs.SchedulableJob;
import de.ronnywalter.eve.model.*;
import de.ronnywalter.eve.service.*;
import lombok.extern.slf4j.Slf4j;
import net.evetech.esi.client.api.AssetsApi;
import net.evetech.esi.client.model.GetCharactersCharacterIdAssets200Ok;
import net.evetech.esi.client.model.GetCorporationsCorporationIdAssets200Ok;
import net.evetech.esi.client.model.PostCharactersCharacterIdAssetsNames200Ok;
import net.evetech.esi.client.model.PostCorporationsCorporationIdAssetsNames200Ok;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpStatusCodeException;

import java.util.*;

@Service
@Slf4j
//@SchedulableJob(scheduleTime = "0 10 * * * *")
@SchedulableJob
public class AssetImportJob extends EsiApiJob {

    private AssetsApi assetsApi = new AssetsApi(getApiClient());
    @Autowired
    private CharacterService characterService;
    @Autowired
    private CorporationService corporationService;
    @Autowired
    private TypeService typeService;
    @Autowired
    private TokenService tokenService;
    @Autowired
    private AssetService assetService;
    @Autowired
    private StructureImportJob structureImportJob;
    @Autowired
    private UniverseService universeService;

    public AssetImportJob() {
        super(AssetImportJob.class.getName());
    }

    private List<Asset> getCorpAssets(Corporation corp, JobData jobData) {
        List<Asset> assets = new ArrayList<>();
        List<GetCorporationsCorporationIdAssets200Ok> response = updatePages(jobData,3, new EsiPagesHandler<GetCorporationsCorporationIdAssets200Ok>() {
            @Override
            public ResponseEntity<List<GetCorporationsCorporationIdAssets200Ok>> get(Integer page) throws HttpStatusCodeException {
                return assetsApi.getCorporationsCorporationIdAssetsWithHttpInfo(corp.getId(), DATASOURCE, null, page, tokenService.getApiToken(corp.getCeoId()));
            }
        });
        if(response != null) {
            response.forEach(r -> {
                assets.add(createCorpAsset(r, corp));
            });
        }
        List<Asset> assetsWithItemNames = assets;//resolveCorpItemNames(corp.getCeoId(), corp.getId(), assets);
        List<Asset> assetsWithLocations = resolveLocations(assetsWithItemNames, corp.getCeoId());

        log.info("Got " + assetsWithLocations.size() + " assets entries for " + corp.getName());
        return assetsWithLocations;
    }

    private List<Asset> getCharacterAssets(EveCharacter character, JobData jobData) throws EveCharacterNotFoundException {
        List<GetCharactersCharacterIdAssets200Ok> result = updatePages(jobData,3, new EsiPagesHandler<GetCharactersCharacterIdAssets200Ok>() {
            @Override
            public ResponseEntity<List<GetCharactersCharacterIdAssets200Ok>> get(Integer page) throws HttpStatusCodeException {
                return assetsApi.getCharactersCharacterIdAssetsWithHttpInfo(character.getId(), DATASOURCE, null, page, tokenService.getApiToken(character.getId()));
            }
        });

        List<Asset> assets = new ArrayList<>();
        for (GetCharactersCharacterIdAssets200Ok r : result) {
            assets.add(createAsset(r,character));
        };

        List<Asset> assetsWithItemNames = resolveItemNames(character, assets);
        List<Asset> assetsWithLocations = resolveLocations(assetsWithItemNames, character.getId());

        log.info("Got " + assetsWithLocations.size() + " assets entries for " + character.getName());
        return assetsWithLocations;
    }

    private List<Asset> resolveLocations(List<Asset> assetsWithItemNames, int characterId) {
        List<Asset> result = new ArrayList<>();
        Map<Long, Asset> assetMap = new HashMap<>();

        assetsWithItemNames.forEach(a -> assetMap.put(a.getId(), a));

        assetsWithItemNames.forEach(a -> {
            if(a.getLocationFlag().equals("Hangar")) {
                Location loc = universeService.getLocation(a.getLocationId());
                if(loc == null && a.getLocationId() >=1000000000000l) {
                    // new Structure, so we trigger an update.
                    log.info("Getting location data for: " + a.toString());
                    loc = structureImportJob.updateStructure(a.getLocationId(), characterId);
                }
                if(loc != null) {
                    a.setLocationName(loc.getName());
                }
            } else if(a.getLocationType().equals("item")) {
                Asset item = assetMap.get(a.getLocationId());
                if(item != null) {
                    a.setLocationName(item.getName());
                }
            }
            result.add(a);
        });

        return result;
    }


    private Asset createAsset(GetCharactersCharacterIdAssets200Ok r, EveCharacter character) throws EveCharacterNotFoundException {
        Asset a = new Asset();
        a.setCharacterId(character.getId());
        a.setBlueprintCopy(r.getIsBlueprintCopy() == null ? false : r.getIsBlueprintCopy().booleanValue());
        a.setLocationFlag(r.getLocationFlag().getValue());
        a.setLocationId(r.getLocationId());
        a.setLocationType(r.getLocationType().getValue());
        a.setQuantity(r.getQuantity());
        a.setSingleton(r.getIsSingleton());
        a.setTypeId(r.getTypeId());
        a.setId(r.getItemId());
        return a;
    }

    private Asset createCorpAsset(GetCorporationsCorporationIdAssets200Ok r, Corporation corp) throws EveCharacterNotFoundException {
        Asset a = new Asset();
        a.setCorpId(corp.getId());
        a.setBlueprintCopy(r.getIsBlueprintCopy() == null ? false : r.getIsBlueprintCopy().booleanValue());
        a.setLocationFlag(r.getLocationFlag().getValue());
        a.setLocationId(r.getLocationId());
        a.setLocationType(r.getLocationType().getValue());
        a.setQuantity(r.getQuantity());
        a.setSingleton(r.getIsSingleton());
        a.setTypeId(r.getTypeId());
        a.setId(r.getItemId());
        return a;
    }

    private List<Asset> resolveItemNames(EveCharacter character, List<Asset> assets) throws EveCharacterNotFoundException {
        Map<Long, Asset> resultMap = new HashMap<>();
        List<List<Asset>> lists = Lists.partition(assets, 1000);

        for (List<Asset> list : lists) {
            Set<Long> itemIds = new HashSet<>();
            list.forEach(asset -> {
                resultMap.put(asset.getId(), asset);
                itemIds.add(asset.getId());
            });
            List<PostCharactersCharacterIdAssetsNames200Ok> itemNames = update(new Update<List<PostCharactersCharacterIdAssetsNames200Ok>>() {
                @Override
                public List<PostCharactersCharacterIdAssetsNames200Ok> update() throws HttpClientErrorException {
                    return assetsApi.postCharactersCharacterIdAssetsNames(character.getId(), itemIds, DATASOURCE, tokenService.getApiToken(character.getId()));
                }
            });

            itemNames.forEach(r -> {
                Asset a = resultMap.get(r.getItemId());
                a.setName(r.getName());
                resultMap.put(a.getId(), a);
            });
        };
        return new ArrayList<>(resultMap.values());
    }

    private List<Asset> resolveCorpItemNames(int characterId, int corpId, List<Asset> assets) throws EveCharacterNotFoundException {
        Map<Long, Asset> resultMap = new HashMap<>();
        List<List<Asset>> lists = Lists.partition(assets, 1000);

        for (List<Asset> list : lists) {
            Set<Long> itemIds = new HashSet<>();
            list.forEach(asset -> {
                resultMap.put(asset.getId(), asset);
                itemIds.add(asset.getId());
            });
            List<PostCorporationsCorporationIdAssetsNames200Ok> itemNames = update(new Update<List<PostCorporationsCorporationIdAssetsNames200Ok>>() {
                @Override
                public List<PostCorporationsCorporationIdAssetsNames200Ok> update() throws HttpClientErrorException {
                    return assetsApi.postCorporationsCorporationIdAssetsNames(corpId, itemIds, DATASOURCE, tokenService.getApiToken(characterId));
                }
            });

            itemNames.forEach(r -> {
                Asset a = resultMap.get(r.getItemId());
                a.setName(r.getName());
                resultMap.put(a.getId(), a);
            });
        };
        return new ArrayList<>(resultMap.values());
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

        List<Corporation> corps = corporationService.getCorporations();
        corps.forEach(c -> {
            Map<String, String> params = new HashMap<>();
            params.put("corpId", "" + c.getId());

            JobData jobData = initJobData(c.getName());

            jobData.setJobParams(params);
            jobDataList.add(jobData);
        });

        return jobDataList;
    }


    @Override
    public void run(JobData jobData) {
        if(jobData.getJobParams().containsKey("characterId")) {
            int characterId = Integer.parseInt(jobData.getJobParams().get("characterId"));
            EveCharacter character = characterService.getEveCharacter(characterId);
            List<Asset> assets = getCharacterAssets(character, jobData);
            assetService.replaceAssetsOfCharacter(character, assets);
        } else if (jobData.getJobParams().containsKey("corpId")) {
            int corpId = Integer.parseInt(jobData.getJobParams().get("corpId"));
            Corporation corporation = corporationService.getCorporation(corpId);
            List<Asset> assets = getCorpAssets(corporation, jobData);
            assetService.replaceAssetsOfCorp(corporation, assets);
        }
    }
}
