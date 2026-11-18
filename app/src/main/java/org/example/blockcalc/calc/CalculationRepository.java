package org.example.blockcalc.calc;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface CalculationRepository extends MongoRepository<Calculation, String> {
    List<Calculation> findByProjectId(String projectId);
}
