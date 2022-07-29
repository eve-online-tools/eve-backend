package de.ronnywalter.eve.jobs.esi;


import com.google.common.collect.Lists;
import de.ronnywalter.eve.exception.EveCharacterNotFoundException;
import de.ronnywalter.eve.jobs.SchedulableJob;
import de.ronnywalter.eve.model.*;
import de.ronnywalter.eve.service.*;
import lombok.extern.slf4j.Slf4j;
import net.evetech.esi.client.api.MarketApi;
import net.evetech.esi.client.api.UniverseApi;
import net.evetech.esi.client.model.GetMarketsRegionIdOrders200Ok;
import net.evetech.esi.client.model.GetMarketsStructuresStructureId200Ok;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpStatusCodeException;

import java.util.*;

@Service
@Slf4j
@SchedulableJob(scheduleTime = "0 0 * * * *")
public class MarketOrderImportJob extends EsiApiJob {

    private final MarketApi marketApi = new MarketApi(getApiClient());
    private final UniverseApi universeApi = new UniverseApi(getApiClient());

    @Autowired
    private MarketOrderService marketOrderService;
    @Autowired
    private UniverseService universeService;
    @Autowired
    private TypeService typeService;
    @Autowired
    private TokenService tokenService;
    @Autowired
    private CharacterService characterService;

    private Map<Integer, Type> types= new HashMap<>();

    public MarketOrderImportJob() {
        super(MarketOrderImportJob.class.getName());
    }

    private void processMarketOrders(int regionId, List<MarketOrder> newMarketOrders) {
        log.info("Process " + newMarketOrders.size() + " orders in region " + regionId);
        List<MarketOrder> existingMarketOrders = marketOrderService.getMarketOrdersForRegion(regionId);

        Map<Long, MarketOrder> existingMarketOrdersAsMap = new HashMap<>();
        Map<Long, MarketOrder> newMarketOrdersAsMap = new HashMap<>();

        Map<Long, MarketOrder> newOrders = new HashMap<>();
        Map<Long, MarketOrder> updatedOrders = new HashMap<>();
        Map<Long, MarketOrder> deletedOrders = new HashMap<>();

        newMarketOrders.forEach(m -> newMarketOrdersAsMap.put(m.getOrderId(), m));
        existingMarketOrders.forEach(m -> existingMarketOrdersAsMap.put(m.getOrderId(), m));

        // find deleted orders
        existingMarketOrdersAsMap.keySet().forEach(k -> {
            if(!newMarketOrdersAsMap.containsKey(k)) {
                deletedOrders.put(k, existingMarketOrdersAsMap.get(k));
            }
        });

        // find new orders
        newMarketOrdersAsMap.keySet().forEach(k -> {
            if(!existingMarketOrdersAsMap.containsKey(k)) {
                newOrders.put(k, newMarketOrdersAsMap.get(k));
            }
        });

        // update existing orders
        newMarketOrdersAsMap.keySet().forEach(k -> {
            MarketOrder oldOrder = existingMarketOrdersAsMap.get(k);
            MarketOrder newOrder = newMarketOrdersAsMap.get(k);

            if(oldOrder != null && newOrder != null && !newOrder.equals(oldOrder)) {
                oldOrder.updateValues(newOrder);
                updatedOrders.put(k, oldOrder);
            }
        });

        log.info("Deleting orders: " + deletedOrders.size() + " in region " + regionId);
        marketOrderService.deleteMarketOrders(Lists.newArrayList(deletedOrders.values()));
        log.info("Storing updated orders: " + updatedOrders.size()  + " in region " + regionId);
        marketOrderService.saveMarketOrders(Lists.newArrayList(updatedOrders.values()));
        log.info("Storing new orders: " + newOrders.size() + " in region " + regionId);
        marketOrderService.saveMarketOrders(Lists.newArrayList(newOrders.values()));
    }


    /*
    @Job(name = "Update marketorders for in region %0", retries = 5)
    public void updateMarketOrdersForRegion(int regionId, int characterId, JobContext jobContext) throws OrderSetNotFoundException {

        OrderSet orderSet = new OrderSet();
        orderSet.setDate(LocalDateTime.now(ZoneOffset.UTC));
        if(regionId == 0) {
            List<Region> regions = universeService.getRegions();
            regions.forEach(r -> orderSet.addRegion(r.getId()));
        } else {
            orderSet.addRegion(regionId);
        }
        marketOrderService.saveOrderSet(orderSet);

        long orderSetId = orderSet.getId();
        final List<JobId> jobIds = new ArrayList<>();
        for(Integer r : orderSet.getRegions()) {
            List<Integer> typeIds = getActiveTypes(r);
            log.info("importing public orders for region " + r);
            for (Integer type : typeIds) {
                jobIds.add(jobScheduler.enqueue(() -> updateMarketOrdersForTypeAndRegion(orderSetId, r, type, characterId, JobContext.Null)));
            }
        }
        jobUtil.waitForJobExecution(jobIds);
        log.info("public orders imported");
    }*/

    private List<Integer> getActiveTypes(int regionId) {
        List<Integer> typeIds = new ArrayList<>();
        ResponseEntity<List<Integer>> response = marketApi.getMarketsRegionIdTypesWithHttpInfo(regionId, DATASOURCE, null, 1);
        Integer xPages = getXPages(response.getHeaders());
        typeIds.addAll(response.getBody());
        if (xPages != null && xPages > 1) {
            for (int i = 2; i <= xPages; i++) {
                List<Integer> ids = marketApi.getMarketsRegionIdTypes(regionId, DATASOURCE, null, i);
                typeIds.addAll(ids);
            }
        }
        return typeIds;
    }

    private MarketOrder processMarketOrder(GetMarketsRegionIdOrders200Ok marketOrdersResponse, int regionId) {
        MarketOrder mo = new MarketOrder();
        mo.setOrderId(marketOrdersResponse.getOrderId());
        mo.setIssuedDate(marketOrdersResponse.getIssued().toLocalDateTime());
        mo.setBuyOrder(marketOrdersResponse.getIsBuyOrder());
        mo.setDuration(marketOrdersResponse.getDuration());
        mo.setVolumeTotal(marketOrdersResponse.getVolumeTotal());
        mo.setVolumeRemain(marketOrdersResponse.getVolumeRemain());
        mo.setPrice(marketOrdersResponse.getPrice());
        mo.setRange(marketOrdersResponse.getRange().getValue());
        mo.setMinVolume(marketOrdersResponse.getMinVolume());
        mo.setRegionId(regionId);

        mo.setLocationId(marketOrdersResponse.getLocationId());
        mo.setTypeId(marketOrdersResponse.getTypeId());

        return mo;
    }

    private MarketOrder processStructureMarketOrder(GetMarketsStructuresStructureId200Ok marketOrdersResponse, Location structure) throws EveCharacterNotFoundException {
        MarketOrder mo = new MarketOrder();
        mo.setOrderId(marketOrdersResponse.getOrderId());
        mo.setIssuedDate(marketOrdersResponse.getIssued().toLocalDateTime());
        mo.setBuyOrder(marketOrdersResponse.getIsBuyOrder());
        mo.setDuration(marketOrdersResponse.getDuration());
        mo.setVolumeTotal(marketOrdersResponse.getVolumeTotal());
        mo.setVolumeRemain(marketOrdersResponse.getVolumeRemain());
        mo.setPrice(marketOrdersResponse.getPrice());
        mo.setRange(marketOrdersResponse.getRange().getValue());
        mo.setMinVolume(marketOrdersResponse.getMinVolume());
        mo.setLocationId(structure.getId());
        mo.setRegionId(structure.getRegionId());

        mo.setLocationId(marketOrdersResponse.getLocationId());
        mo.setTypeId(marketOrdersResponse.getTypeId());

        return mo;
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
        //regionsToProcess.add(10000002);
        regionsToProcess.forEach(r -> {
            JobData jobData = initJobData("market orders region: " + String.valueOf(r));
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
        log.info("Updating market orders for region: " + regionId);
        Region r = universeService.getRegion(regionId);

        log.info("Processing region: " + r.getName());
        List<GetMarketsRegionIdOrders200Ok> marketOrdersResponses = updatePages(jobData, 3, new EsiPagesHandler<GetMarketsRegionIdOrders200Ok>() {
            @Override
            public ResponseEntity<List<GetMarketsRegionIdOrders200Ok>> get(Integer page) throws HttpClientErrorException {
                return marketApi.getMarketsRegionIdOrdersWithHttpInfo("all", r.getId(), DATASOURCE, null, page, null);
            }
        });
        List<MarketOrder> marketOrders = new ArrayList<>();
        Set<Long> ids = new HashSet<>();

        for(GetMarketsRegionIdOrders200Ok marketsRegionIdOrders200Ok : marketOrdersResponses) {
            marketOrders.add(processMarketOrder(marketsRegionIdOrders200Ok, r.getId()));
        }
        processMarketOrders(r.getId(), marketOrders);

        List<EveCharacter> chars = characterService.getEveCharacters();
        int count = 0;
        if(chars.size() > 0) {
            List<Location> structures = universeService.getAllowedStructuresWithMarketForRegion(r.getId());
            structures.forEach(s -> {
                log.info("Getting orders for structure " + s.getId() + " (" + s.getName() + ")");
                List<GetMarketsStructuresStructureId200Ok> response = updatePages(jobData, 3, new EsiPagesHandler<GetMarketsStructuresStructureId200Ok>() {
                    @Override
                    public ResponseEntity<List<GetMarketsStructuresStructureId200Ok>> get(Integer page) throws HttpStatusCodeException {
                        return marketApi.getMarketsStructuresStructureIdWithHttpInfo(s.getId(), DATASOURCE, null, page, tokenService.getApiToken(chars.get(0).getId()));
                    }
                });
                response.forEach(getMarketsStructuresStructureId200Ok -> {
                    processStructureMarketOrder(getMarketsStructuresStructureId200Ok, s);
                });

            });
        }
        //jobData.setNextExecutionTime(getExpiryDate(marketOrdersResponses));
    }
}
