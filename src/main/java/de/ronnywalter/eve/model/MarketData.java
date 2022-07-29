package de.ronnywalter.eve.model;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Data
public class MarketData {

    private Type type;

    private List<MarketOrder> buyOrders;
    private List<MarketOrder> sellOrders;

    private double averagePrice5d;
    private double averagePrice20d;
    private double averageVolume5d;
    private double averageVolume20d;

    public long getBuyVolumeRemain() {
        Long sum = buyOrders.stream().mapToLong(MarketOrder::getVolumeRemain).sum();
        return sum;
    }

    public long getSellVolumeRemain() {
        Long sum = sellOrders.stream().mapToLong(MarketOrder::getVolumeRemain).sum();
        return sum;
    }

    public int getBuyOrderCount() {
        return buyOrders.size();
    }

    public int getSellOrderCount() {
        return sellOrders.size();
    }

    public double getSellPrice() {
        if(getSellOrderCount() > 0) {
            return sellOrders.get(0).getPrice();
        } else {
            return -1;
        }
    }

    public double getBuyPrice() {
        if(getBuyOrderCount() > 0) {
            return buyOrders.get(0).getPrice();
        } else {
            return -1;
        }
    }

    public int getNumberOfSellOrderUpdates() {
        return getNumberOfOrderUpdates(sellOrders, 3);
    }

    public int getNumberOfSellOrderUpdates(int days) {
        return getNumberOfOrderUpdates(sellOrders, days);
    }

    public int getNumberOfBuyOrderUpdates() {
        return getNumberOfOrderUpdates(buyOrders, 1);
    }

    public int getNumberOfBuyOrderUpdates(int days) {
        return getNumberOfOrderUpdates(buyOrders, days);
    }

    private double getAveragePrice(List<MarketHistory> history, int days) {
        double value = 0;

        if(history.size() > 0) {

            Map<LocalDate, MarketHistory> values = new HashMap();

            history.forEach(h -> {
                values.put(h.getDate(), h);
            });

            LocalDate lastDate = history.get(0).getDate();
            for (int i = 0; i < days; i++) {
                MarketHistory h = values.get(lastDate);
                if(h != null) {
                    System.out.println(String.format("%,.2f", h.getAverage()));
                    value += h.getAverage();
                    lastDate = lastDate.minusDays(1);
                } else {
                    return -1;
                }
            }
        } else {
            return -1;
        }
        System.out.println("Sum: " + String.format("%,.2f", value));
        return value / days;
    }

    private int getAverageVolume(List<MarketHistory> history, int days) {
        int value = 0;

        if(history.size() > 0) {

            Map<LocalDate, MarketHistory> values = new HashMap();

            history.forEach(h -> {
                values.put(h.getDate(), h);
            });

            LocalDate lastDate = history.get(0).getDate();
            for (int i = 0; i < days; i++) {
                MarketHistory h = values.get(lastDate);
                if(h != null) {
                    value += h.getVolume();
                    lastDate = lastDate.minusDays(1);
                } else {
                    return -1;
                }
            }
        } else {
            return -1;
        }
        return value / days;
    }



    private int getNumberOfOrderUpdates(List<MarketOrder> marketOrders, int days) {
        LocalDateTime now = LocalDateTime.now();
        final LocalDateTime date = now.minusDays(days);

        int count = 0;

        for (MarketOrder o : marketOrders) {
            if(o.getIssuedDate().isAfter(date)) {
                count += 1;
            }
        }
        return count;
    }

    public int getVolumeForPrice(double maxPrice) {
        int volume = 0;
        for (MarketOrder o : sellOrders) {
            if(o.getPrice() <= maxPrice) {
                volume += o.getVolumeRemain();
            }
        }
        return volume;
    }

    public double getAverageSellPriceForVolume(int volume) {
        if(volume > 0) {
            Map<Integer, Double> prices = new HashMap<>();
            int needToBuy = volume;
            for (MarketOrder o : sellOrders) {
                if(o.getVolumeRemain() <= needToBuy) {
                    prices.put(o.getVolumeRemain(), o.getPrice());
                    needToBuy -= o.getVolumeRemain();
                } else {
                    prices.put(needToBuy, o.getPrice());
                    needToBuy -= needToBuy;
                }
            }
            double value = 0;
            int sum = 0;
            for (Integer count : prices.keySet() ) {
                Double price = prices.get(count);
                value += count * price;
                sum += count;
            }
            return value / sum;
        } else {
            return 0;
        }
    }



}
