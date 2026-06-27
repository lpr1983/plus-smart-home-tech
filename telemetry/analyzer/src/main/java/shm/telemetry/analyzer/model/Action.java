package shm.telemetry.analyzer.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "actions")
public class Action {
    @Id
    private Long id;
    private String type;
    private Integer value;
}
