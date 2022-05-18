package com.velokofi.events.persistence;

import com.velokofi.events.model.ActivityStats;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ActivityStatsRepository extends MongoRepository<ActivityStats, String> {

}
