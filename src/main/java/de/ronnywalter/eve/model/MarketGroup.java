package de.ronnywalter.eve.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Transient;
import java.util.List;
import java.util.Set;

@Entity
@Getter
@Setter
public class MarketGroup extends DBEntity {

    @Id
    private int id;
    @Column(columnDefinition="TEXT")
    private String description;
    private String name;

    private Integer parentId;

    @Transient
    private List<MarketGroup> children;

    private boolean hasTypes;
    private Integer iconId;
    private String icon;
}
