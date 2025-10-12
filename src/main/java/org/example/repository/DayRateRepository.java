package org.example.repository;

import org.example.model.DayRate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DayRateRepository extends JpaRepository<DayRate, Integer> {
}
