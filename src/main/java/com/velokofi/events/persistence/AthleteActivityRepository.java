package com.velokofi.events.persistence;

import com.velokofi.events.model.AthleteActivity;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface AthleteActivityRepository extends MongoRepository<AthleteActivity, String> {

}
