package de.ronnywalter.eve.model.dbscheduler;

import lombok.Data;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "scheduled_tasks")
@Data
public class ScheduledTask {
    @EmbeddedId
    private ScheduledTaskId id;

    @Lob
    @Column(name = "task_data")
    @Type(type="org.hibernate.type.BinaryType")
    private byte[] taskData;

    @Column(name = "execution_time", nullable = false)
    private OffsetDateTime executionTime;

    @Column(name = "picked", nullable = false)
    private Boolean picked = false;

    @Column(name = "picked_by", columnDefinition="TEXT")
    private String pickedBy;

    @Column(name = "last_success")
    private OffsetDateTime lastSuccess;

    @Column(name = "last_failure")
    private OffsetDateTime lastFailure;

    @Column(name = "consecutive_failures")
    private Integer consecutiveFailures;

    @Column(name = "last_heartbeat")
    private OffsetDateTime lastHeartbeat;

    @Column(name = "version", nullable = false)
    private Long version;
}