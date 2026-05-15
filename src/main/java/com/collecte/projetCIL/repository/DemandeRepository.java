package com.collecte.projetCIL.repository;

import com.collecte.projetCIL.models.Demande;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DemandeRepository extends JpaRepository<Demande, Long> {
}
