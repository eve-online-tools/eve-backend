package de.ronnywalter.eve.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;

@Entity
@Data
@EqualsAndHashCode (callSuper = true)
public class Location extends DBEntity {

    @Id
    private Long id;

    @Enumerated(EnumType.STRING)
    private LocationType locationType;

    private String name;

    private Integer typeId;

    private int ownerCorpId;

    private int solarsystemId;
    private int constellationId;
    private int regionId;

    private Boolean accessForbidden;
    private Boolean hasMarket;

}
