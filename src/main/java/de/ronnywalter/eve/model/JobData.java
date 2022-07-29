package de.ronnywalter.eve.model;

import lombok.*;

import javax.persistence.*;
import java.io.Serializable;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class JobData extends DBEntity implements Serializable {

    @Id
    @GeneratedValue
    private long id;

    private String jobClassName;
    private String jobName;

    private long runCount;

    private Instant nextExecutionTime;
    private UUID nextJobId;
    private Instant lastExecutionTime;

    private boolean isCronJob;
    private String cronExpression;
    private Long lastDuration;

    private Boolean enabled;

    @ElementCollection (fetch = FetchType.EAGER)
    private Map<String, String> jobParams;

    public void incrementRunCount() {
        this.runCount++;
    }


    public String getNextTaskInstance() {
        long nextRunCount = runCount + 1;
        return getJobName() + "-" + nextRunCount;
    }

}
