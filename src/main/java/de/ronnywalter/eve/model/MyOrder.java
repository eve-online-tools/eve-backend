package de.ronnywalter.eve.model;

import lombok.*;
import org.apache.commons.lang.time.DurationFormatUtils;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.TemporalUnit;
import java.util.concurrent.TimeUnit;


@Data
@Entity
@EqualsAndHashCode(callSuper = true )
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MyOrder extends DBEntity {

    @Id
    private long id;

    private int duration;

    private boolean isBuyOrder;
    private LocalDateTime issuedDate;

    private int minVolume;
    private double price;
    private int volumeTotal;
    private int volumeRemain;

    private String range;

    private int typeId;
    private String type;

    private long locationId;

    private int characterId;
    private Integer corpId;
    private Integer wallet_division;
    private Double escrow;
    private boolean corpOrder;
    private String state;

    public String getDurationAsString() {
        Duration d = Duration.between(LocalDateTime.now(), issuedDate.plusDays(duration));
        return DurationFormatUtils.formatDuration(d.toMillis(), "d'd' H'h' m'm");

    }


}