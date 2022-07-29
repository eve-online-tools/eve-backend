package de.ronnywalter.eve.model;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Embeddable;
import java.io.Serializable;

@AllArgsConstructor
@EqualsAndHashCode
@NoArgsConstructor
@Getter
@Embeddable
public class TradeCandidateId implements Serializable {
    private int configId;
    private int typeId;
}