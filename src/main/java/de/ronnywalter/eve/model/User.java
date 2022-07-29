package de.ronnywalter.eve.model;

import lombok.Getter;
import lombok.Setter;


import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.Instant;

@Entity
@Getter
@Setter
@Table(name = "application_user")
public class User extends DBEntity {

    @Id
    private int id;

    private String name;

    private Instant lastLogin;

}
