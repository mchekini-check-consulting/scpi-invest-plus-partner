package fr.formationacademy.scpiinvestpluspartner.repository;

import fr.formationacademy.scpiinvestpluspartner.entity.Investment;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InvestmentRepository extends MongoRepository<Investment, Integer> {
}
