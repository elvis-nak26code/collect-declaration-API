package com.collecte.projetCIL.repository;

import com.collecte.projetCIL.enums.StatutDemandeAcces;
import com.collecte.projetCIL.models.DemandeAcces;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DemandeAccesRepository extends JpaRepository<DemandeAcces, Long> {
    List<DemandeAcces> findByStatutDemandeAcces(StatutDemandeAcces statut);
}
