package fr.formationacademy.scpiinvestpluspartner.repository;


import fr.formationacademy.scpiinvestpluspartner.entity.ScpiDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ScpiRepository extends MongoRepository<ScpiDocument, String> {
    Optional<ScpiDocument> findByName(String name);
}