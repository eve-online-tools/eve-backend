package de.ronnywalter.eve.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

@Entity
@Data
@EqualsAndHashCode (callSuper = true)
public class Corporation extends DBEntity {

    @Id
    private int id;

    @ManyToOne
    private User user;

    private String name;
    private String ticker;
    private int memberCount;

    private int ceoId;

    private String logo;
}
