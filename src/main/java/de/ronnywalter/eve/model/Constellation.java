package de.ronnywalter.eve.model;

import lombok.*;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode (callSuper = true )
public class Constellation extends DBEntity {

    @Id
    private Integer id;
    private String name;

    private int regionId;

}
