package de.ronnywalter.eve.model;

import lombok.*;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Entity
@EqualsAndHashCode (callSuper = true )
public class Region extends DBEntity {

    @Id
    private Integer id;
    private String name;


    @ToString.Exclude
    @Column(columnDefinition="TEXT")
    private String description;

}
