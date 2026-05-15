package com.collecte.projetCIL.repository;

import com.collecte.projetCIL.models.DeclarationNormale;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DeclarationNormaleRepository extends JpaRepository<DeclarationNormale, Long> {
}
