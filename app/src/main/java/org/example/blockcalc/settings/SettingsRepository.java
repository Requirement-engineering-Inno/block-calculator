package org.example.blockcalc.settings;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface SettingsRepository extends MongoRepository<AppSettings, String> {
}
