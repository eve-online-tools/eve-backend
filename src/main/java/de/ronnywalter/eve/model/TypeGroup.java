package de.ronnywalter.eve.model;

import lombok.*;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode (callSuper = true )
public class TypeGroup extends DBEntity {

    @Id
    private Integer id;
    private String name;

    private int categoryId;

}
