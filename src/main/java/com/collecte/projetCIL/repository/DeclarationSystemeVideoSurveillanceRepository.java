package com.collecte.projetCIL.repository;

import com.collecte.projetCIL.models.DeclarationSystemeVideoSurveillance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DeclarationSystemeVideoSurveillanceRepository extends JpaRepository<DeclarationSystemeVideoSurveillance, Long> {
}
