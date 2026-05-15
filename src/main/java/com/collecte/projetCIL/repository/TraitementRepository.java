package com.collecte.projetCIL.repository;

import com.collecte.projetCIL.models.Traitement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TraitementRepository extends JpaRepository<Traitement, Long> {
}
