package com.collecte.projetCIL.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.collecte.projetCIL.models.UtilisateurMetier;

@Repository
public interface UtilisateurMetierRepository extends JpaRepository<UtilisateurMetier, Long> {
    Optional<UtilisateurMetier> findByEmail(String email);
}