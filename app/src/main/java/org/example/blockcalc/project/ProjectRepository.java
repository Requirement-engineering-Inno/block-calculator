package org.example.blockcalc.project;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface ProjectRepository extends MongoRepository<Project, String> {
    Optional<Project> findBySystemTrue();

    Optional<Project> findByName(String name);
}
