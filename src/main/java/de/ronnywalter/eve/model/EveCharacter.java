package de.ronnywalter.eve.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;


@Getter
@Setter
//@Entity
@ToString
@EqualsAndHashCode(callSuper = true )
@Entity
public class EveCharacter extends DBEntity implements Serializable {

    @Id
    private Integer id;
    private String name;

    @ManyToOne
    private User user;

    private Integer allianceId;
    private Integer corporationId;
    private String corporationName;
    private String corporationTicker;

    private Float securityStatus;

    private String portrait64;
    private String portrait128;
    private String portrait256;
    private String portrait512;
    private String corpLogo;

    private Long locationId;
    private String locationName;

    private Integer solarSystemId;
    private String solarSystemName;

    @Column(columnDefinition="TEXT")
    private String apiToken;
    @Column(columnDefinition="TEXT")
    private String refreshToken;
    private LocalDateTime expiryDate;
    private String clientId;
}
