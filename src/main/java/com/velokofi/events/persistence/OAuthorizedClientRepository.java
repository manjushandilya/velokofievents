package com.velokofi.events.persistence;

import com.velokofi.events.model.OAuthorizedClient;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface OAuthorizedClientRepository extends MongoRepository<OAuthorizedClient, String> {

}
