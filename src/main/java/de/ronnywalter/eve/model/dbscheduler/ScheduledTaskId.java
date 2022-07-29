package de.ronnywalter.eve.model.dbscheduler;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.Hibernate;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Entity;
import javax.persistence.Lob;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
@Getter
@Setter
public class ScheduledTaskId implements Serializable {
    private static final long serialVersionUID = 4895663492033160890L;

    @Column(name = "task_name", nullable = false, columnDefinition="TEXT")
    private String taskName;

    @Lob
    @Column(name = "task_instance", nullable = false, columnDefinition="TEXT")
    private String taskInstance;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        ScheduledTaskId entity = (ScheduledTaskId) o;
        return Objects.equals(this.taskInstance, entity.taskInstance) &&
                Objects.equals(this.taskName, entity.taskName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(taskInstance, taskName);
    }

}