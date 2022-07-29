package de.ronnywalter.eve.model;

import lombok.*;

import javax.persistence.Entity;
import javax.persistence.Id;


@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode (callSuper = true )
@Entity
public class SolarSystem extends DBEntity {

    @Id
    private Integer id;
    private String name;

    private double securityStatus;

    private int constellationId;

    private int regionId;

}
