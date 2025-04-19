package edu.uth.nurseborn.repositories;

import edu.uth.nurseborn.models.WebIncome;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WebIncomeRepository extends JpaRepository<WebIncome, Long> {
}