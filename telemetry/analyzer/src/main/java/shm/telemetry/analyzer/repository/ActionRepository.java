package shm.telemetry.analyzer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import shm.telemetry.analyzer.model.Action;

public interface ActionRepository extends JpaRepository<Action, Long> {
}
