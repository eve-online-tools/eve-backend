package de.ronnywalter.eve.jobs.esi;

import de.ronnywalter.eve.jobs.SchedulableJob;
import de.ronnywalter.eve.model.Corporation;
import de.ronnywalter.eve.model.EveCharacter;
import de.ronnywalter.eve.model.JobData;
import de.ronnywalter.eve.model.JournalEntry;
import de.ronnywalter.eve.service.CharacterService;
import de.ronnywalter.eve.service.CorporationService;
import de.ronnywalter.eve.service.TokenService;
import de.ronnywalter.eve.service.WalletService;
import lombok.extern.slf4j.Slf4j;
import net.evetech.esi.client.api.WalletApi;
import net.evetech.esi.client.model.GetCharactersCharacterIdWalletJournal200Ok;
import net.evetech.esi.client.model.GetCorporationsCorporationIdWallets200Ok;
import net.evetech.esi.client.model.GetCorporationsCorporationIdWalletsDivisionJournal200Ok;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpStatusCodeException;

import java.util.*;

@Service
@Slf4j
@SchedulableJob
public class WalletJournalImportJob extends EsiApiJob {

    private final WalletApi walletApi = new WalletApi(getApiClient());

    @Autowired
    private CharacterService characterService;
    @Autowired
    private CorporationService corporationService;
    @Autowired
    private WalletService walletService;
    @Autowired
    private TokenService tokenService;

    public WalletJournalImportJob() {
        super(WalletJournalImportJob.class.getName());
    }

    private JournalEntry createJournalEntry(GetCharactersCharacterIdWalletJournal200Ok r, EveCharacter character) {
        JournalEntry je = new JournalEntry();
        je.setId(r.getId());
        je.setCharacterId(character.getId());
        je.setCharacterName(character.getName());
        je.setAmount(r.getAmount());
        je.setBalance(r.getBalance());
        je.setContextId(r.getContextId());
        je.setContextType(r.getContextIdType() == null ? null : r.getContextIdType().getValue());
        je.setDate(r.getDate().toLocalDateTime());
        je.setDescription(r.getDescription());
        je.setParty1(r.getFirstPartyId());
        //je.setParty1Name(resolveCharacterName(r.getFirstPartyId()));
        je.setReason(r.getReason());
        je.setRefType(r.getRefType().getValue());
        je.setParty2(r.getSecondPartyId());
        //je.setParty2Name(resolveCharacterName(r.getSecondPartyId()));
        je.setTax(r.getTax());
        je.setTax_receiver_id(r.getTaxReceiverId());
        return je;
    }

    private JournalEntry createCorpJournalEntry(GetCorporationsCorporationIdWalletsDivisionJournal200Ok r, Integer division, Corporation corp) {
        JournalEntry je = new JournalEntry();
        je.setId(r.getId());
        je.setDivision(division);
        je.setCorpId(corp.getId());
        je.setCorpName(corp.getName());
        je.setCorpTicker(corp.getTicker());
        je.setAmount(r.getAmount());
        je.setBalance(r.getBalance());
        je.setContextId(r.getContextId());
        je.setContextType(r.getContextIdType() == null ? null : r.getContextIdType().getValue());
        je.setDate(r.getDate().toLocalDateTime());
        je.setDescription(r.getDescription());
        je.setParty1(r.getFirstPartyId());
        //je.setParty1Name(resolveCharacterName(r.getFirstPartyId()));
        je.setReason(r.getReason());
        je.setRefType(r.getRefType().getValue());
        je.setParty2(r.getSecondPartyId());
        //je.setParty2Name(resolveCharacterName(r.getSecondPartyId()));
        je.setTax(r.getTax());
        je.setTax_receiver_id(r.getTaxReceiverId());
        return je;
    }

    @Override
    public List<JobData> init() {
        List<JobData> jobDataList = new ArrayList<>();
        List<EveCharacter> characters = characterService.getEveCharacters();
        characters.forEach(c -> {
            JobData jobData = initJobData("Journal: " + c.getName());
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
                Map<String, String> params = new HashMap<>();
                params.put("corpId", "" + c.getId());
                int division = w.getDivision();
                params.put("division", "" + division);
                JobData jobData = initJobData("Journal: " + c.getTicker() + ", division: " + division);
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
            processCharacterJournal(characterId, jobData);
        } else if(jobData.getJobParams().containsKey("corpId")) {
            int corpId = Integer.parseInt(jobData.getJobParams().get("corpId"));
            int division = Integer.parseInt(jobData.getJobParams().get("division"));
            processCorpJournal(corpId, division, jobData);
        }
    }

    private void processCorpJournal(int corpId, int division, JobData jobData) {
        Set<Long> existingJournalIds = walletService.getJournalIds();
        List<JournalEntry> journalEntries = new ArrayList<>();
        Corporation c = corporationService.getCorporation(corpId);
        log.info("Getting journal for " + c.getName());

        List<GetCorporationsCorporationIdWalletsDivisionJournal200Ok> result = new ArrayList<>();

        List<GetCorporationsCorporationIdWalletsDivisionJournal200Ok> response = updatePages(jobData, 3, new EsiPagesHandler<GetCorporationsCorporationIdWalletsDivisionJournal200Ok>() {
            @Override
            public ResponseEntity<List<GetCorporationsCorporationIdWalletsDivisionJournal200Ok>> get(Integer page) throws HttpStatusCodeException {
                return walletApi.getCorporationsCorporationIdWalletsDivisionJournalWithHttpInfo(c.getId(), division, DATASOURCE, null, page, tokenService.getApiToken(c.getCeoId()));
            }
        });

        result.addAll(response);

        result.forEach(r -> {
            JournalEntry je = createCorpJournalEntry(r, division, c);
            if(!existingJournalIds.contains(je.getId())) {
                journalEntries.add(je);
            }
        });

        log.info("Saving " + journalEntries.size() + " new journal entries");
        walletService.saveJournalEntries(journalEntries);
    }

    private void processCharacterJournal(int characterId, JobData jobData) {
        Set<Long> existingJournalIds = walletService.getJournalIds();
        List<JournalEntry> journalEntries = new ArrayList<>();
        List<EveCharacter> characters = characterService.getEveCharacters();

        EveCharacter c = characterService.getEveCharacter(characterId);
        log.info("Getting journal for " + c.getName());
        List<GetCharactersCharacterIdWalletJournal200Ok> result = updatePages(jobData,3, new EsiPagesHandler<GetCharactersCharacterIdWalletJournal200Ok>() {
            @Override
            public ResponseEntity<List<GetCharactersCharacterIdWalletJournal200Ok>> get(Integer page) throws HttpStatusCodeException {
                return walletApi.getCharactersCharacterIdWalletJournalWithHttpInfo(characterId, DATASOURCE, null, page, tokenService.getApiToken(characterId));
            }
        });
        if (result != null) {
            result.forEach(r -> {
                JournalEntry je = createJournalEntry(r,c);
                if(!existingJournalIds.contains(je.getId())) {
                    journalEntries.add(je);
                }
            });
        }
        log.info("Saving " + journalEntries.size() + " new journal entries");
        walletService.saveJournalEntries(journalEntries);
    }
}
