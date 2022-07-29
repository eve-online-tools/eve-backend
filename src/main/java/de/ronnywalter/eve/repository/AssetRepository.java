package de.ronnywalter.eve.repository;

import de.ronnywalter.eve.model.Asset;
import org.springframework.data.repository.CrudRepository;


public interface AssetRepository extends CrudRepository<Asset, Long> {
    void deleteByCharacterId(Integer id);

    void deleteByCorpId(Integer id);
}
