package de.ronnywalter.eve.model;

import lombok.*;

import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import java.io.Serializable;
import java.time.LocalDate;


@Entity
@Data
@NoArgsConstructor
@EqualsAndHashCode (callSuper = true)
public class MarketHistory extends DBEntity {

    @EmbeddedId
    private MarketHistoryId id;

    private double average;
    private double highest;
    private double lowest;
    private long orderCount;
    private long volume;

    public MarketHistory(int regionId, int typeId, LocalDate date) {
        id = new MarketHistoryId(regionId, typeId, date);
    }

    public LocalDate getDate() {
        return this.id.getDate();
    }
    public int getType() { return this.id.getTypeId(); }
}

@AllArgsConstructor
@EqualsAndHashCode
@NoArgsConstructor
@Getter
@Embeddable
class MarketHistoryId implements Serializable {

    private int regionId;

    private int typeId;
    private LocalDate date;
}