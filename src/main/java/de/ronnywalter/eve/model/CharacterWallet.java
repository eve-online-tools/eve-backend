package de.ronnywalter.eve.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.io.Serializable;


@Entity
@Data
@EqualsAndHashCode (callSuper = true)
public class CharacterWallet extends DBEntity implements Serializable {

    @Id
    private int id;

    private int characterId;
    private Double value;
}
