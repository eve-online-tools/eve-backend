package de.ronnywalter.eve.model;

import lombok.*;

import javax.persistence.Entity;
import javax.persistence.Id;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode (callSuper = true )
public class TypeCategory extends DBEntity {

    @Id
    private Integer id;
    private String name;

}
