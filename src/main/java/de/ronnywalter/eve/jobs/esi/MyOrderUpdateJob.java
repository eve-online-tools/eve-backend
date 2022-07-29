package de.ronnywalter.eve.jobs.esi;

import com.google.common.collect.Lists;
import de.ronnywalter.eve.jobs.SchedulableJob;
import de.ronnywalter.eve.model.Corporation;
import de.ronnywalter.eve.model.EveCharacter;
import de.ronnywalter.eve.model.JobData;
import de.ronnywalter.eve.model.MyOrder;
import de.ronnywalter.eve.service.*;
import lombok.extern.slf4j.Slf4j;
import net.evetech.esi.client.api.MarketApi;
import net.evetech.esi.client.model.GetCharactersCharacterIdOrders200Ok;
import net.evetech.esi.client.model.GetCharactersCharacterIdOrdersHistory200Ok;
import net.evetech.esi.client.model.GetCorporationsCorporationIdOrders200Ok;
import net.evetech.esi.client.model.GetCorporationsCorporationIdOrdersHistory200Ok;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpStatusCodeException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j

//@SchedulableJob(scheduleTime = "0 0,30 * * * *")
@SchedulableJob
public class MyOrderUpdateJob extends EsiApiJob {

    @Autowired
    private CharacterService characterService;
    @Autowired
    private CorporationService corporationService;
    @Autowired
    private MyOrderService myOrderService;
    @Autowired
    private TokenService tokenService;
    @Autowired
    private TypeService typeService;
    @Autowired
    private UniverseService universeService;
    private MarketApi marketApi = new MarketApi(getApiClient());

    public MyOrderUpdateJob() {
        super(MyOrderUpdateJob.class.getName());
    }

    private List<MyOrder> getConsolidatedOrders(List<MyOrder> characterOrders, List<MyOrder> corpOrders) {
        Map<Long, MyOrder> charOrderMap = new HashMap<>();
        Map<Long, MyOrder> corpOrderMap = new HashMap<>();

        corpOrders.forEach(c -> corpOrderMap.put(c.getId(), c));
        characterOrders.forEach(c -> {
            if(!corpOrderMap.containsKey(c.getId())) {
                charOrderMap.put(c.getId(), c);
            }
        });
        List<MyOrder> result = new ArrayList<>();
        result.addAll(corpOrderMap.values());
        result.addAll(charOrderMap.values());
        return result;
    }


    private MyOrder createOrder(GetCharactersCharacterIdOrders200Ok r, EveCharacter character) {

        MyOrder o = MyOrder.builder()
                .id(r.getOrderId())
                .corpOrder(r.getIsCorporation())
                .isBuyOrder(r.getIsBuyOrder() == null ? false : r.getIsBuyOrder().booleanValue())
                .characterId(character.getId())
                .duration(r.getDuration())
                .issuedDate(r.getIssued().toLocalDateTime())
                .escrow(r.getEscrow() != null ? r.getEscrow() : 0d)
                .locationId(r.getLocationId())
                .minVolume(r.getMinVolume() != null ? r.getMinVolume() : 0)
                .price(r.getPrice())
                .range(r.getRange().getValue())
                .typeId(r.getTypeId())
                .type(typeService.getType(r.getTypeId()).getName())
                .volumeRemain(r.getVolumeRemain())
                .volumeTotal(r.getVolumeTotal())
                .build();

        o.setState("open");
        return o;
    }

    private MyOrder createHistOrder(GetCharactersCharacterIdOrdersHistory200Ok r, EveCharacter character) {

        MyOrder o = MyOrder.builder()
                .id(r.getOrderId())
                .corpOrder(r.getIsCorporation())
                .isBuyOrder(r.getIsBuyOrder() == null ? false : r.getIsBuyOrder().booleanValue())
                .characterId(character.getId())
                .duration(r.getDuration())
                .issuedDate(r.getIssued().toLocalDateTime())
                .escrow(r.getEscrow() != null ? r.getEscrow() : 0d)
                .locationId(r.getLocationId())
                .minVolume(r.getMinVolume() != null ? r.getMinVolume() : 0)
                .price(r.getPrice())
                .range(r.getRange().getValue())
                .typeId(r.getTypeId())
                .type(typeService.getType(r.getTypeId()).getName())
                .volumeRemain(r.getVolumeRemain())
                .volumeTotal(r.getVolumeTotal())
                .build();
        o.setState(r.getState().getValue());

        return o;
    }

    private MyOrder createCorpOrder(GetCorporationsCorporationIdOrders200Ok r, Corporation corporation) {

        MyOrder o = MyOrder.builder()
                .id(r.getOrderId())
                .corpOrder(true)
                .isBuyOrder(r.getIsBuyOrder() == null ? false : r.getIsBuyOrder().booleanValue())
                .characterId(r.getIssuedBy())
                .duration(r.getDuration())
                .issuedDate(r.getIssued().toLocalDateTime())
                .escrow(r.getEscrow() != null ? r.getEscrow() : 0d)
                .locationId(r.getLocationId())
                .minVolume(r.getMinVolume() != null ? r.getMinVolume() : 0)
                .price(r.getPrice())
                .range(r.getRange().getValue())
                .typeId(r.getTypeId())
                .type(typeService.getType(r.getTypeId()).getName())
                .volumeRemain(r.getVolumeRemain())
                .volumeTotal(r.getVolumeTotal())
                .wallet_division(r.getWalletDivision())
                .corpId(corporation.getId())
                .build();

        o.setState("open");
        return o;
    }

    private MyOrder createHistCorpOrder(GetCorporationsCorporationIdOrdersHistory200Ok r, Corporation corporation) {

        MyOrder o = MyOrder.builder()
                .id(r.getOrderId())
                .corpOrder(true)
                .isBuyOrder(r.getIsBuyOrder() == null ? false : r.getIsBuyOrder().booleanValue())
                .characterId(r.getIssuedBy())
                .duration(r.getDuration())
                .issuedDate(r.getIssued().toLocalDateTime())
                .escrow(r.getEscrow() != null ? r.getEscrow() : 0d)
                .locationId(r.getLocationId())
                .minVolume(r.getMinVolume() != null ? r.getMinVolume() : 0)
                .price(r.getPrice())
                .range(r.getRange().getValue())
                .typeId(r.getTypeId())
                .type(typeService.getType(r.getTypeId()).getName())
                .volumeRemain(r.getVolumeRemain())
                .volumeTotal(r.getVolumeTotal())
                .wallet_division(r.getWalletDivision())
                .corpId(corporation.getId())
                .build();
        o.setState(r.getState().getValue());
        return o;
    }

    @Override
    public List<JobData> init() {
        JobData jobData = initJobData("character and corp orders");
        return Lists.newArrayList(jobData);
    }

    @Override
    public void run(JobData jobData) {
        List<EveCharacter> characters = characterService.getEveCharacters();
        List<Corporation> corps = corporationService.getCorporations();
        List<MyOrder> characterOrders = new ArrayList<>();
        List<MyOrder> corpOrders = new ArrayList<>();



        characters.forEach(c -> {
            ResponseEntity<List<GetCharactersCharacterIdOrders200Ok>> ordersResponse = update(new Update<ResponseEntity<List<GetCharactersCharacterIdOrders200Ok>>>() {
                @Override
                public ResponseEntity<List<GetCharactersCharacterIdOrders200Ok>> update() throws HttpClientErrorException {
                    return marketApi.getCharactersCharacterIdOrdersWithHttpInfo(c.getId(), DATASOURCE, null, tokenService.getApiToken(c.getId()));
                }
            });

            ordersResponse.getBody().forEach(r -> {
                characterOrders.add(createOrder(r, c));
            });


            List<GetCharactersCharacterIdOrdersHistory200Ok> response = updatePages(jobData, 3, new EsiPagesHandler<GetCharactersCharacterIdOrdersHistory200Ok>() {
                @Override
                public ResponseEntity<List<GetCharactersCharacterIdOrdersHistory200Ok>> get(Integer page) throws HttpStatusCodeException {
                    return marketApi.getCharactersCharacterIdOrdersHistoryWithHttpInfo(c.getId(), DATASOURCE, null, page, tokenService.getApiToken(c.getId()));
                }
            });
            response.forEach(g -> {
                characterOrders.add(createHistOrder(g, c));
            });
        });


        corps.forEach(corporation -> {
            List<GetCorporationsCorporationIdOrders200Ok> ordersResponse = updatePages(jobData,3, new EsiPagesHandler<GetCorporationsCorporationIdOrders200Ok>() {
                @Override
                public ResponseEntity<List<GetCorporationsCorporationIdOrders200Ok>> get(Integer page) throws HttpStatusCodeException {
                    return marketApi.getCorporationsCorporationIdOrdersWithHttpInfo(corporation.getId(), DATASOURCE, null, page, tokenService.getApiToken(corporation.getCeoId()));
                }
            });
            ordersResponse.forEach(r -> {
                corpOrders.add(createCorpOrder(r, corporation));
            });

            List<GetCorporationsCorporationIdOrdersHistory200Ok> ordersHistory = updatePages(jobData, 3, new EsiPagesHandler<GetCorporationsCorporationIdOrdersHistory200Ok>() {
                @Override
                public ResponseEntity<List<GetCorporationsCorporationIdOrdersHistory200Ok>> get(Integer page) throws HttpStatusCodeException {
                    return marketApi.getCorporationsCorporationIdOrdersHistoryWithHttpInfo(corporation.getId(), DATASOURCE, null, page, tokenService.getApiToken(corporation.getCeoId()));
                }
            });
            ordersHistory.forEach(r -> {
                corpOrders.add(createHistCorpOrder(r, corporation));
            });

        });


        List<MyOrder> orders = getConsolidatedOrders(characterOrders, corpOrders);
        myOrderService.saveOrders(orders);
    }

}


