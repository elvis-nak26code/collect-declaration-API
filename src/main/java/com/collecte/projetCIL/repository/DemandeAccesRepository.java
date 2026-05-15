package com.collecte.projetCIL.repository;

import com.collecte.projetCIL.models.DemandeAcces;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DemandeAccesRepository extends JpaRepository<DemandeAcces, Long> {
}
