package com.collecte.projetCIL.repository;

import com.collecte.projetCIL.models.UtilisateurMetier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UtilisateurMetierRepository extends JpaRepository<UtilisateurMetier, Long> {
}
