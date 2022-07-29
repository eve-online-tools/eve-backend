package de.ronnywalter.eve.model;

import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.Column;
import javax.persistence.EntityListeners;
import javax.persistence.MappedSuperclass;
import java.io.Serializable;
import java.time.Instant;

@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@EqualsAndHashCode
public class DBEntity implements Serializable {
    @Column(name = "created_date", nullable = false, updatable = false)
    @CreatedDate
    @EqualsAndHashCode.Exclude
    private Instant createdDate;

    @Column(name = "modified_date")
    @LastModifiedDate
    @EqualsAndHashCode.Exclude
    private Instant modifiedDate;
}
