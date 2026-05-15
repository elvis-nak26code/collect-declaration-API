package com.collecte.projetCIL.repository;

import com.collecte.projetCIL.models.CIL;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CILRepository extends JpaRepository<CIL, Long> {
}
