package de.ronnywalter.eve.service.scanning;

import de.ronnywalter.eve.model.MarketOrder;

import java.util.Comparator;

public class MarketOrderPriceDescComparator implements Comparator<MarketOrder> {
    @Override
    public int compare(MarketOrder o1, MarketOrder o2) {
        return Double.compare(o1.getPrice(),o2.getPrice()) * -1;
    }
}
