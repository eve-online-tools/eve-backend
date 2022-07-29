package de.ronnywalter.eve.model;

import lombok.*;

import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import java.io.Serializable;

@Entity
@Data
@EqualsAndHashCode (callSuper = true)
@NoArgsConstructor
public class CorpWallet extends DBEntity {

    @EmbeddedId
    private CorpWalletId id;
    private Double value;

    public CorpWallet(int corp, int division) {
        this.id = new CorpWalletId(corp, division);
    }
}

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@Embeddable
class CorpWalletId implements Serializable {

    private int corpId;
    private int division;
}
