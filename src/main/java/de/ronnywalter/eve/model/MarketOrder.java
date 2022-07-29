package de.ronnywalter.eve.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;
import java.time.LocalDateTime;

@Data
//@Entity
@EqualsAndHashCode(callSuper = true )
@ToString
@NoArgsConstructor

@Table(
        indexes = {
                @Index(name = "idx_location", columnList = "locationId"),
                @Index(name = "idx_region_type", columnList = "regionId, typeId")
        }
)
@Entity
public class MarketOrder extends DBEntity {

    @Id
    private long orderId;

    private int duration;

    private boolean isBuyOrder;
    private LocalDateTime issuedDate;

    private int minVolume;
    private double price;
    private int volumeTotal;
    private int volumeRemain;

    private String range;

    private int regionId;

    private int typeId;

    private long locationId;

    public void updateValues(MarketOrder newOrder) {
        if(newOrder != null) {
            this.duration = newOrder.duration;
            this.isBuyOrder = newOrder.isBuyOrder;
            this.issuedDate = newOrder.issuedDate;
            this.minVolume = newOrder.minVolume;
            this.price = newOrder.price;
            this.volumeTotal = newOrder.volumeTotal;
            this.range = newOrder.range;
            this.typeId = newOrder.typeId;
            this.regionId = newOrder.regionId;
            this.locationId = newOrder.locationId;
        }
    }
}
