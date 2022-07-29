package de.ronnywalter.eve.jobs.esi;

import de.ronnywalter.eve.exception.EveCharacterNotFoundException;
import de.ronnywalter.eve.jobs.SchedulableJob;
import de.ronnywalter.eve.model.*;
import de.ronnywalter.eve.service.*;
import lombok.extern.slf4j.Slf4j;
import net.evetech.esi.client.api.WalletApi;
import net.evetech.esi.client.model.GetCharactersCharacterIdWalletTransactions200Ok;
import net.evetech.esi.client.model.GetCorporationsCorporationIdWallets200Ok;
import net.evetech.esi.client.model.GetCorporationsCorporationIdWalletsDivisionTransactions200Ok;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import java.util.*;

@Service
@Slf4j
@SchedulableJob
public class WalletTransactionsImportJob extends EsiApiJob {

    private final WalletApi walletApi = new WalletApi(getApiClient());

    @Autowired
    private CharacterService characterService;
    @Autowired
    private CorporationService corporationService;
    @Autowired
    private WalletService walletService;
    @Autowired
    private TokenService tokenService;
    @Autowired
    private TypeService typeService;
    @Autowired
    private UniverseService universeService;

    public WalletTransactionsImportJob() {
        super(WalletTransactionsImportJob.class.getName());
    }

    private Transaction createCharacterTransaction(GetCharactersCharacterIdWalletTransactions200Ok transactionsResponse, EveCharacter character) throws EveCharacterNotFoundException {
        Transaction t = new Transaction();
        t.setTransactionId(transactionsResponse.getTransactionId());
        t.setDate(transactionsResponse.getDate().toLocalDateTime());
        t.setBuy(transactionsResponse.getIsBuy());
        t.setClientId(transactionsResponse.getClientId());
        t.setClientName(resolveCharacterName(Long.valueOf(t.getClientId()).intValue()));
        t.setPersonal(transactionsResponse.getIsPersonal());
        t.setJournalRefId(transactionsResponse.getJournalRefId());
        t.setUnitPrice(transactionsResponse.getUnitPrice());
        t.setQuantity(transactionsResponse.getQuantity());
        t.setTypeId(transactionsResponse.getTypeId());
        t.setType(typeService.getType(t.getTypeId()).getName());
        t.setCharacterId(character.getId());
        t.setCharacterName(character.getName());
        if(!t.isPersonal()) {
        	t.setCorpId(character.getCorporationId());
        	t.setCorpName(character.getCorporationName());
        	t.setCorpTicker(character.getCorporationTicker());
        }

        t.setLocationId(transactionsResponse.getLocationId());
        Location loc = universeService.getLocation(transactionsResponse.getLocationId());
        if(loc == null && transactionsResponse.getLocationId() >=100000000) {
            // new Structure, so we trigger an update.
            //loc = universeImportService.importStructure(t.getLocationId(), characterId);
        }

        return t;

    }

    private Transaction createCorpTransaction(GetCorporationsCorporationIdWalletsDivisionTransactions200Ok transactionsResponse, Corporation corp, int division) throws EveCharacterNotFoundException {
        Transaction t = new Transaction();
        t.setTransactionId(transactionsResponse.getTransactionId());
        t.setDate(transactionsResponse.getDate().toLocalDateTime());
        t.setBuy(transactionsResponse.getIsBuy());
        t.setClientId(transactionsResponse.getClientId());
        t.setClientName(resolveCharacterName(Long.valueOf(t.getClientId()).intValue()));
        t.setJournalRefId(transactionsResponse.getJournalRefId());
        t.setUnitPrice(transactionsResponse.getUnitPrice());
        t.setQuantity(transactionsResponse.getQuantity());
        t.setDivision(division);

        t.setTypeId(transactionsResponse.getTypeId());
        t.setType(typeService.getType(t.getTypeId()).getName());
        t.setCorpId(corp.getId());
        t.setCorpName(corp.getName());
        t.setCorpTicker(corp.getTicker());
        t.setLocationId(transactionsResponse.getLocationId());
        Location loc = universeService.getLocation(transactionsResponse.getLocationId());
        if(loc == null && transactionsResponse.getLocationId() >=100000000) {
            // new Structure, so we trigger an update.
            //loc = universeImportService.importStructure(t.getLocationId(), characterId);
        }
        return t;
    }

    @Override
    public List<JobData> init() {
        List<JobData> jobDataList = new ArrayList<>();
        List<EveCharacter> characters = characterService.getEveCharacters();
        characters.forEach(c -> {
            JobData jobData = initJobData("Transactions: " + c.getName());
            Map<String, String> params = new HashMap<>();
            params.put("characterId", "" + c.getId());
            jobData.setJobParams(params);
            jobDataList.add(jobData);
        });

        List<Corporation> corps = corporationService.getCorporations();
        corps.forEach(c -> {

            ResponseEntity<List<GetCorporationsCorporationIdWallets200Ok>> wallets = update(new Update<ResponseEntity<List<GetCorporationsCorporationIdWallets200Ok>>>() {
                @Override
                public ResponseEntity<List<GetCorporationsCorporationIdWallets200Ok>> update() throws HttpClientErrorException {
                    return walletApi.getCorporationsCorporationIdWalletsWithHttpInfo(c.getId(), DATASOURCE, null, tokenService.getApiToken(c.getCeoId()));
                }
            });
            wallets.getBody().forEach(w -> {
                int division = w.getDivision();
                JobData jobData = initJobData("Transactions: " + c.getTicker() + ", division: " + division);
                Map<String, String> params = new HashMap<>();
                params.put("corpId", "" + c.getId());
                params.put("division", "" + division);
                jobData.setJobParams(params);
                jobDataList.add(jobData);
            });
        });
        return jobDataList;
    }

    @Override
    public void run(JobData jobData) {

        if(jobData.getJobParams().containsKey("characterId")) {
            int characterId = Integer.parseInt(jobData.getJobParams().get("characterId"));
            processCharacterTransactions(characterId, jobData);
        } else if(jobData.getJobParams().containsKey("corpId")) {
            int corpId = Integer.parseInt(jobData.getJobParams().get("corpId"));
            int division = Integer.parseInt(jobData.getJobParams().get("division"));
            processCorpTransactions(corpId, division, jobData);
        }
    }

    private void processCorpTransactions(int corpId, int division, JobData jobData) {
        Set<Long> existingTransactionIds = walletService.getTransactionIds();
        List<Transaction> transactions = new ArrayList<>();
        Corporation c = corporationService.getCorporation(corpId);
        log.info("getting  transactions for corp " + c.getName() + " and " + division);

        List<GetCorporationsCorporationIdWalletsDivisionTransactions200Ok> result = new ArrayList<>();

        ResponseEntity<List<GetCorporationsCorporationIdWalletsDivisionTransactions200Ok>> response = update(new Update<ResponseEntity<List<GetCorporationsCorporationIdWalletsDivisionTransactions200Ok>>>() {
            @Override
            public ResponseEntity<List<GetCorporationsCorporationIdWalletsDivisionTransactions200Ok>> update() throws HttpClientErrorException {
                return walletApi.getCorporationsCorporationIdWalletsDivisionTransactionsWithHttpInfo(c.getId(), division, DATASOURCE, null, null, tokenService.getApiToken(c.getCeoId()));
            }
        });

        result.addAll(response.getBody());

        result.forEach(r -> {
            Transaction transaction = createCorpTransaction(r, c, division);
            if(!existingTransactionIds.contains(transaction.getTransactionId())) {
                transactions.add(transaction);
            }
        });

        walletService.saveTransactions(transactions);
        jobData.setNextExecutionTime(getExpiryDate(response.getHeaders()));
    }

    private void processCharacterTransactions(int characterId, JobData jobData) {
        Set<Long> existingTransactionIds = walletService.getTransactionIds();
        List<Transaction> transactions = new ArrayList<>();
        EveCharacter c = characterService.getEveCharacter(characterId);
        log.info("getting  transactions for character " + c.getName());
        ResponseEntity<List<GetCharactersCharacterIdWalletTransactions200Ok>> result = update(new Update<ResponseEntity<List<GetCharactersCharacterIdWalletTransactions200Ok>>>() {
            @Override
            public ResponseEntity<List<GetCharactersCharacterIdWalletTransactions200Ok>> update() throws HttpClientErrorException {
                return walletApi.getCharactersCharacterIdWalletTransactionsWithHttpInfo(c.getId(), DATASOURCE, null, null,tokenService.getApiToken(c.getId()));                }
        });
        if (result != null) {
            for (GetCharactersCharacterIdWalletTransactions200Ok r : result.getBody()) {
                Transaction transaction = createCharacterTransaction(r, c);
                if(transaction.isPersonal() && !existingTransactionIds.contains(transaction.getTransactionId())) {
                    transactions.add(transaction);
                }
            };
        }
        walletService.saveTransactions(transactions);
        jobData.setNextExecutionTime(getExpiryDate(result.getHeaders()));
    }

}
