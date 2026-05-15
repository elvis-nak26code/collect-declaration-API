package com.collecte.projetCIL.repository;

import com.collecte.projetCIL.models.DeclarationAutorisation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DeclarationAutorisationRepository extends JpaRepository<DeclarationAutorisation, Long> {
}
