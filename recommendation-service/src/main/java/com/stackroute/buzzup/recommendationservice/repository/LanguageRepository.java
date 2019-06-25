package com.stackroute.buzzup.recommendationservice.repository;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

import com.stackroute.buzzup.recommendationservice.model.Language;



@Repository
public interface LanguageRepository extends Neo4jRepository<Language, Long> {
	public Language findByName(String name);
}
