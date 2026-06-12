package com.collecte.projetCIL.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.collecte.projetCIL.models.Traitement;

@Repository
public interface TraitementRepository extends JpaRepository<Traitement, Long> {
}
