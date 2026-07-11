package com.collecte.projetCIL.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.collecte.projetCIL.models.CleApiCil;

@Repository
public interface CleApiCilRepository extends JpaRepository<CleApiCil, Long> {

    Optional<CleApiCil> findByCleHacheeAndActifTrue(String cleHachee);
}
