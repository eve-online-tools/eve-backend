package de.ronnywalter.eve.model.stats;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class PriceStats {
    int typeId;
    double price;
}
