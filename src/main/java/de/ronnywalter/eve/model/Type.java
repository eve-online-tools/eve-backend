package de.ronnywalter.eve.model;

import lombok.*;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;


@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode (callSuper = true )
public class Type extends DBEntity {

    @Id
    private Integer id;

    private String name;
    private double volume;
    private double packagedVolume;
    private double capacity;

    @Column(columnDefinition="TEXT")
    private String description;
    private Integer graphicId;
    private Integer iconId;

    private Integer marketGroupId;
    private String marketGroup;
    private double mass;
    private int portionSize;
    private boolean published;
    private double radius;

    private int groupId;
    private int categoryId;
}
