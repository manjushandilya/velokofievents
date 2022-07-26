package com.velokofi.events.persistence;

import com.velokofi.events.model.ActivityStatistics;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface BeatYesterdayAugToOct2022Repository extends MongoRepository<ActivityStatistics, String> {

}
