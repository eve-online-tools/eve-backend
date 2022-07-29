package de.ronnywalter.eve.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.time.LocalDateTime;

@Entity
@Data
@EqualsAndHashCode(callSuper = true )
public class JournalEntry extends DBEntity {

    @Id
    private long id;

    private double amount;
    private double balance;
    private Long contextId;
    private String contextType;
    private LocalDateTime date;
    private String description;
    private int party1;
    private String party1Name;
    private String reason;
    private String refType;
    private int party2;
    private String party2Name;

    private Double tax;
    private Integer tax_receiver_id;

    private Integer characterId;
    private String characterName;
    private Integer corpId;
    private String corpName;
    private String corpTicker;
    private Integer division;

}
