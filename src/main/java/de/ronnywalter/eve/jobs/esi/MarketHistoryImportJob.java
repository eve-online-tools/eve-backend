package de.ronnywalter.eve.jobs.esi;

import de.ronnywalter.eve.jobs.SchedulableJob;
import de.ronnywalter.eve.model.JobData;
import de.ronnywalter.eve.model.MarketHistory;
import de.ronnywalter.eve.model.Region;
import de.ronnywalter.eve.model.Type;
import de.ronnywalter.eve.service.MarketHistoryService;
import de.ronnywalter.eve.service.TypeService;
import de.ronnywalter.eve.service.UniverseService;
import lombok.extern.slf4j.Slf4j;
import net.evetech.esi.client.api.MarketApi;
import net.evetech.esi.client.api.UniverseApi;
import net.evetech.esi.client.model.GetMarketsRegionIdHistory200Ok;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpStatusCodeException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Service
@Slf4j
@SchedulableJob(scheduleTime = "0 30 12 * * *")
public class MarketHistoryImportJob extends EsiApiJob {


    private MarketApi marketApi = new MarketApi(getApiClient());
    private final UniverseApi universeApi = new UniverseApi(getApiClient());
    @Autowired
    private MarketHistoryService marketHistoryService;
    @Autowired
    private TypeService typeService;
    @Autowired
    private UniverseService universeService;

    private final Executor executor = Executors.newFixedThreadPool(30);

    protected boolean disabled = true;

    public MarketHistoryImportJob() {
        super(MarketHistoryImportJob.class.getName());
    }

    private void importMarketHistoryForRegionAndType(Region region, Type type) {
        ResponseEntity<List<GetMarketsRegionIdHistory200Ok>> response = update(new Update<ResponseEntity<List<GetMarketsRegionIdHistory200Ok>>>() {
            @Override
            public ResponseEntity<List<GetMarketsRegionIdHistory200Ok>> update() throws HttpClientErrorException {
                return marketApi.getMarketsRegionIdHistoryWithHttpInfo(region.getId(), type.getId(), DATASOURCE, null);
            }
        });

        List<MarketHistory> mhList = new ArrayList<>();
        response.getBody().forEach(getMarketsRegionIdHistory200Ok -> {
            mhList.add(processMarketHistory(getMarketsRegionIdHistory200Ok, region, type));
        });
        log.debug("Saving " + mhList.size() + " records for type " + type.getName() + " in region " + region.getName());
        marketHistoryService.saveAllMarketHistory(mhList);
    }

    
    private void importMarketHistoryForRegion(Region r) {
    /*    log.info("Getting markethistory for region " + r.getId() );
        List<Integer> typeResponse = updatePages(3, new EsiPagesHandler<Integer>() {
            @Override
            public ResponseEntity<List<Integer>> get(Integer page) throws HttpStatusCodeException {
                return marketApi.getMarketsRegionIdTypesWithHttpInfo(r.getId(), DATASOURCE, null, page);
            }
        });
        if(typeResponse != null) {
            List<CompletableFuture<Void>> futures = new ArrayList<>();
            for(int typeId : typeResponse) {
                CompletableFuture<Void> f = CompletableFuture.supplyAsync(() -> {
                    ResponseEntity<List<GetMarketsRegionIdHistory200Ok>> response = update(new Update<ResponseEntity<List<GetMarketsRegionIdHistory200Ok>>>() {
                        @Override
                        public ResponseEntity<List<GetMarketsRegionIdHistory200Ok>> update() throws HttpClientErrorException {
                            return marketApi.getMarketsRegionIdHistoryWithHttpInfo(r.getId(), typeId, DATASOURCE, null);
                        }
                    });
                    List<MarketHistory> mhList = new ArrayList<>();
                    response.getBody().forEach(getMarketsRegionIdHistory200Ok -> {
                        mhList.add(processMarketHistory(getMarketsRegionIdHistory200Ok, r.getId(), typeId));
                    });
                    return mhList;
                }, executor).thenAccept(marketHistory -> {
                    log.info("Storing marketHistory-Items: " + marketHistory.size());
                    marketHistoryService.saveAllMarketHistory(marketHistory);
                });
                futures.add(f);
            };
            futures.forEach(CompletableFuture::join);
        }

     */
    }

    private MarketHistory processMarketHistory(GetMarketsRegionIdHistory200Ok r, Region region, Type type) {
        MarketHistory m = new MarketHistory(region.getId(), type.getId(), r.getDate());
        m.setAverage(r.getAverage());
        m.setHighest(r.getHighest());
        m.setLowest(r.getLowest());
        m.setOrderCount(r.getOrderCount());
        m.setVolume(r.getVolume());
        return m;
    }

    @Override
    public List<JobData> init() {
        List<JobData> jobDataList= new ArrayList<>();
        List<Integer> regionsToProcess = new ArrayList<>();

        ResponseEntity<List<Integer>> ids = update(new Update<ResponseEntity<List<Integer>>>() {
            @Override
            public ResponseEntity<List<Integer>> update() throws HttpClientErrorException {
                return universeApi.getUniverseRegionsWithHttpInfo(DATASOURCE, null);
            }
        });
        regionsToProcess.addAll(ids.getBody());
        regionsToProcess.forEach(r -> {
            JobData jobData = initJobData("market history region: " + String.valueOf(r));
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
        log.info("Updating market history for region: " + regionId);

        List<Integer> publishedTypes = typeService.getPublishedTypeIds();
        Region r = universeService.getRegion(regionId);

        Map<Integer, LocalDate> maxDates = marketHistoryService.getMaxDatesAsMap(regionId);

        List<Integer> typeResponse = updatePages(jobData, 3, new EsiPagesHandler<Integer>() {
            @Override
            public ResponseEntity<List<Integer>> get(Integer page) throws HttpStatusCodeException {
                return marketApi.getMarketsRegionIdTypesWithHttpInfo(r.getId(), DATASOURCE, null, page);
            }
        });
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        if(typeResponse != null) {
            typeResponse.forEach(typeId -> {
                if(publishedTypes.contains(typeId)) {
                    Type type = typeService.getType(typeId);
                    CompletableFuture<Void> f = CompletableFuture.supplyAsync(() -> {
                        log.debug("Getting history for type: " + type.getName() + " and region " + regionId);
                        ResponseEntity<List<GetMarketsRegionIdHistory200Ok>> response = update(new Update<ResponseEntity<List<GetMarketsRegionIdHistory200Ok>>>() {
                            @Override
                            public ResponseEntity<List<GetMarketsRegionIdHistory200Ok>> update() throws HttpClientErrorException {
                                return marketApi.getMarketsRegionIdHistoryWithHttpInfo(r.getId(), typeId, DATASOURCE, null);
                            }
                        });
                        List<MarketHistory> mhList = new ArrayList<>();
                        if(response != null) {
                            response.getBody().forEach(getMarketsRegionIdHistory200Ok -> {
                                MarketHistory mh = processMarketHistory(getMarketsRegionIdHistory200Ok, r, type);
                                if(!maxDates.containsKey(typeId) || mh.getDate().isAfter(maxDates.get(type.getId()))) {
                                    mhList.add(mh);
                                }
                            });
                        } else {
                            log.warn("No response for type " + typeId + " in region " + r.getName());
                        }
                        return mhList;
                    }, executor).thenAccept(marketHistory -> {
                        log.debug("Storing " + marketHistory.size() + " marketHistory-Items in region " + r.getName() + " and type " + typeId);
                        marketHistoryService.saveAllMarketHistory(marketHistory);
                        //marketHistoryService.saveNewMarketHistoryForRegion(regionId, typeId, marketHistory);
                    });
                    futures.add(f);
                }
            });
        }
        futures.forEach(CompletableFuture::join);
        log.info("finished markethistory-import for region " + regionId);
    }
}
