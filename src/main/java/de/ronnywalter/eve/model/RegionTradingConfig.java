package de.ronnywalter.eve.model;

import lombok.*;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;


@Getter
@Setter
@Builder
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class RegionTradingConfig extends DBEntity {

//List<Type> types, Location from, Location to, int daysVolume, double minMargin, int minVolumeBuyRegion, int minVolumeSellRegion, int ordersModifyDays, int maxModifiedOrders

        @Id
        @GeneratedValue
        @Setter(value = AccessLevel.NONE)
        private int id;

        private String name;

        private int buyRegionId;
        private int sellRegionId;

        private Long buyLocationId;
        private Long sellLocationId;

        private double minMargin;

        private int minVolumeBuyRegion;
        private int minVolumeSellRegion;

        private int daysModifiedOrders;

        private int maxModifiedOrdersWithinDays;

}
