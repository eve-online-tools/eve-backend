package de.ronnywalter.eve.model;

import lombok.*;

import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class TradeCandidate extends DBEntity {

    @EmbeddedId
    private TradeCandidateId id;

    private int buyRegionId;
    private int sellRegionId;

    private double currentSellPrice;
    private double currentBuyPrice;
    private double currentProfitPct;
    private double currentProfitPerItem;
    private double maxBuyPrice;
    private int possibleVolume;
    private double averageBuyPrice;
    private double averageSellPrice;
    private double averageProfit;
    private double averageProfitPct;
    private double averageProfitPerItem;
    private double averageProfitAtCurrentSellPrices;
    private double averageProfitPctAtCurrentSellPrices;
    private double averageVolumeSellRegion;

    private int modifiedOrdersInSellRegion;
    private double cargo;

    public TradeCandidate(int config, int typeId) { this.id = new TradeCandidateId(config, typeId);};
}

