package de.ronnywalter.eve.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

@AllArgsConstructor
@Getter
public class MarketHistoryMaxDate {

    private int typeId;
    private LocalDate date;

}
