package shm.telemetry.analyzer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import shm.telemetry.analyzer.model.Condition;

public interface ConditionRepository extends JpaRepository<Condition, Long> {
}