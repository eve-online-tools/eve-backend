package de.ronnywalter.eve.jobs.esi;

import com.google.common.collect.Lists;
import de.ronnywalter.eve.dto.MarketGroupDTO;
import de.ronnywalter.eve.jobs.SchedulableJob;
import de.ronnywalter.eve.jobs.sde.MarketGroupSDEService;
import de.ronnywalter.eve.model.*;
import de.ronnywalter.eve.service.TypeService;
import lombok.extern.slf4j.Slf4j;
import net.evetech.esi.client.api.MarketApi;
import net.evetech.esi.client.api.UniverseApi;
import net.evetech.esi.client.model.GetMarketsGroupsMarketGroupIdOk;
import net.evetech.esi.client.model.GetUniverseCategoriesCategoryIdOk;
import net.evetech.esi.client.model.GetUniverseGroupsGroupIdOk;
import net.evetech.esi.client.model.GetUniverseTypesTypeIdOk;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Service
@Slf4j
@SchedulableJob(scheduleTime = "0 15 11 * * *")
public class TypeImportJob extends EsiApiJob {

    @Autowired
    private TypeService typeService;

    @Autowired
    private MarketGroupSDEService marketGroupSDEService;

    @Autowired
    private ModelMapper modelMapper;

    private final UniverseApi universeApi = new UniverseApi(getApiClient());
    private final MarketApi marketApi = new MarketApi();
    private final Executor executor = Executors.newFixedThreadPool(10);
    private final Executor executorGroups = Executors.newFixedThreadPool(20);
    private final Executor executorTypes = Executors.newFixedThreadPool(30);

    private static final Map<Integer, TypeCategory> knownTypeCategories = new HashMap<>();
    private static final Map<Integer, TypeGroup> knownTypeGroups = new HashMap<>();

    public TypeImportJob() {
        super(TypeImportJob.class.getName());
    }

    private List<Integer> getGroupIds() {
        List<Integer> groupIds = new ArrayList<>();

        ResponseEntity<List<Integer>> groupIdResponse = universeApi.getUniverseGroupsWithHttpInfo(DATASOURCE, null, 1);
        Integer xPages = getXPages(groupIdResponse.getHeaders());
        groupIds.addAll(groupIdResponse.getBody());

        if (xPages != null && xPages > 1) {
            for (int i = 2; i <= xPages; i++) {
                List<Integer> ids = universeApi.getUniverseGroups(DATASOURCE, null, i);
                groupIds.addAll(ids);
            }
        }
        return groupIds;
    }

    private List<Integer> getTypeIds() {
        List<Integer> ids = new ArrayList<>();

        ResponseEntity<List<Integer>> response = universeApi.getUniverseTypesWithHttpInfo(DATASOURCE, null, 1);
        Integer xPages = getXPages(response.getHeaders());
        ids.addAll(response.getBody());

        if (xPages != null && xPages > 1) {
            for (int i = 2; i <= xPages; i++) {
                final int page = i;
                response = universeApi.getUniverseTypesWithHttpInfo(DATASOURCE, null, i);
                ids.addAll(response.getBody());
            }
        }
        return ids;
    }

    private List<Integer> getCategoryIds() {
        List<Integer> categoryIds = new ArrayList<>();
        ResponseEntity<List<Integer>> categoryIdResponse = universeApi.getUniverseCategoriesWithHttpInfo(DATASOURCE, null);
        categoryIds.addAll(categoryIdResponse.getBody());
        return categoryIds;
    }

    @Transactional
    public void updateTypeCategory(Integer id) {
        GetUniverseCategoriesCategoryIdOk response = update(new Update<GetUniverseCategoriesCategoryIdOk>() {
            @Override
            public GetUniverseCategoriesCategoryIdOk update() throws HttpClientErrorException {
                return universeApi.getUniverseCategoriesCategoryId(id, LANGUAGE, DATASOURCE, null, LANGUAGE);
            }
        });

        TypeCategory c = typeService.getTypeCategory(id);
        if (c == null) {
            c = new TypeCategory();
            c.setId(id);
        }
        c.setName(response.getName());
        typeService.saveTypeCategory(c);
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        for (Integer groupId : response.getGroups()) {
            TypeCategory finalC = c;
            CompletableFuture<Void> f = CompletableFuture.runAsync(() -> updateTypeGroup(groupId, finalC), executorGroups);
            futures.add(f);
        }
        ;
        futures.forEach(CompletableFuture::join);
        log.info("finished category: " + c.getName());
    }

    public void updateTypeGroup(Integer id, TypeCategory typeCategory) {
        GetUniverseGroupsGroupIdOk response = update(new Update<GetUniverseGroupsGroupIdOk>() {
            @Override
            public GetUniverseGroupsGroupIdOk update() throws HttpClientErrorException {
                return universeApi.getUniverseGroupsGroupId(id, LANGUAGE, DATASOURCE, null, LANGUAGE);
            }
        });

        TypeGroup tg = typeService.getTypeGroup(id);
        if (tg == null) {
            tg = new TypeGroup();
            tg.setId(id);
        }
        tg.setCategoryId(typeCategory.getId());
        tg.setName(response.getName());
        typeService.saveTypeGroup(tg);

        List<CompletableFuture<Void>> futures = new ArrayList<>();
        TypeGroup finalTg = tg;
        response.getTypes().forEach(typeId -> {
            CompletableFuture<Void> f = CompletableFuture.runAsync(() -> updateType(typeId, finalTg), executorTypes);
            futures.add(f);
        });
        futures.forEach(CompletableFuture::join);

        log.info("finished category/group: " + typeCategory.getName() + "/" + tg.getName());
    }


    public void updateType(Integer id, TypeGroup tg) {
        ResponseEntity<GetUniverseTypesTypeIdOk> responseEntity = update(new Update<ResponseEntity<GetUniverseTypesTypeIdOk>>() {
            @Override
            public ResponseEntity<GetUniverseTypesTypeIdOk> update() throws HttpClientErrorException {
                return universeApi.getUniverseTypesTypeIdWithHttpInfo(id, LANGUAGE, DATASOURCE, null, LANGUAGE);
            }
        });

        Type type = typeService.getType(id);
        if (type == null) {
            type = new Type();
            type.setId(id);
        }
        GetUniverseTypesTypeIdOk response = responseEntity.getBody();
        type.setId(response.getTypeId());
        type.setName(response.getName());
        try {
            type.setIconId(response.getIconId());
        } catch (NullPointerException e) {
            // NPE if there is no IconId
        }
        ;
        type.setPackagedVolume(response.getPackagedVolume());
        type.setVolume(response.getVolume());
        type.setPortionSize(response.getPortionSize());
        type.setGroupId(tg.getId());
        type.setCategoryId(tg.getCategoryId());
        type.setCapacity(response.getCapacity());
        type.setDescription(response.getDescription());
        type.setGraphicId(response.getGraphicId());
        type.setIconId(response.getIconId());
        type.setMass(response.getMass());
        type.setMarketGroupId(response.getMarketGroupId());
        type.setPublished(response.getPublished());
        type.setRadius(response.getRadius());
        log.debug("Saving type: " + type.getName());
        typeService.saveType(type);

    }

    @Override
    public List<JobData> init() {
        JobData jobData = initJobData("types");
        return Lists.newArrayList(jobData);
    }


    @Override
    public void run(JobData jobData) {
        log.info("Getting Market-Groups");
        updateMarketGroupsFromSDE();

        log.info("Updating typeCategories");
        List<Integer> categoryIds = new ArrayList();
        categoryIds.addAll(getCategoryIds());

        List<CompletableFuture<Void>> futures = new ArrayList<>();
        categoryIds.forEach(r -> {
            CompletableFuture<Void> f = CompletableFuture.runAsync(() -> updateTypeCategory(r), executor);
            futures.add(f);
        });
        futures.forEach(CompletableFuture::join);
    }

    private void updateMarketGroupsFromSDE() {
        List<MarketGroupDTO> marketGroupDTOS = marketGroupSDEService.getMarketGroups();

        marketGroupDTOS.forEach(dto -> {
            MarketGroup mg = modelMapper.map(dto, MarketGroup.class);
            typeService.saveMarketGroup(mg);
        });


    }

    private void updateMarketGroupsFromEsi() {

        List<Integer> groupIds = new ArrayList<>();
        ResponseEntity<List<Integer>> groupIdResponse = update(new Update<ResponseEntity<List<Integer>>>() {
            @Override
            public ResponseEntity<List<Integer>> update() throws HttpClientErrorException {
                return marketApi.getMarketsGroupsWithHttpInfo(DATASOURCE, null);
            }
        });
        groupIds.addAll(groupIdResponse.getBody());

        List<CompletableFuture<Void>> futures = new ArrayList<>();

        for (Integer groupId : groupIds) {
            CompletableFuture<Void> f = CompletableFuture.runAsync(() -> retrieveMarketGroupFromEsi(groupId), executorGroups);
            futures.add(f);
        }
        futures.forEach(CompletableFuture::join);
    }

    private void retrieveMarketGroupFromEsi(int id) {
        ResponseEntity<GetMarketsGroupsMarketGroupIdOk> response = update(new Update<ResponseEntity<GetMarketsGroupsMarketGroupIdOk>>() {
            @Override
            public ResponseEntity<GetMarketsGroupsMarketGroupIdOk> update() throws HttpClientErrorException {
                return marketApi.getMarketsGroupsMarketGroupIdWithHttpInfo(id, LANGUAGE, DATASOURCE, null, LANGUAGE);
            }
        });
        if(response != null) {
            GetMarketsGroupsMarketGroupIdOk r = response.getBody();
            MarketGroup mg = new MarketGroup();
            mg.setId(id);
            mg.setName(r.getName());
            mg.setDescription(r.getDescription());
            mg.setParentId(r.getParentGroupId());
            typeService.saveMarketGroup(mg);
        }
    }
}

