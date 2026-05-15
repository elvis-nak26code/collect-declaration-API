package com.collecte.projetCIL.repository;

import com.collecte.projetCIL.models.HistoriqueDeclaration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HistoriqueDeclarationRepository extends JpaRepository<HistoriqueDeclaration, Long> {
}
