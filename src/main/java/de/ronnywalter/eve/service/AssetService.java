package de.ronnywalter.eve.service;

import com.google.common.collect.Lists;
import de.ronnywalter.eve.model.Asset;
import de.ronnywalter.eve.model.Corporation;
import de.ronnywalter.eve.model.EveCharacter;
import de.ronnywalter.eve.repository.AssetRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class AssetService {

    private final AssetRepository assetRepository;

    public Asset saveAsset(Asset a) {
        return assetRepository.save(a);
    }

    public List<Asset> saveAssets(List<Asset> a) {
        return Lists.newArrayList(assetRepository.saveAll(a));
    }

    @Transactional
    public List<Asset> replaceAssetsOfCharacter(EveCharacter character, List<Asset> assets) {
        assetRepository.deleteByCharacterId(character.getId());
        return Lists.newArrayList(assetRepository.saveAll(assets));
    }

    @Transactional
    public List<Asset> replaceAssetsOfCorp(Corporation corp, List<Asset> assets) {
        assetRepository.deleteByCorpId(corp.getId());
        return Lists.newArrayList(assetRepository.saveAll(assets));
    }
}
