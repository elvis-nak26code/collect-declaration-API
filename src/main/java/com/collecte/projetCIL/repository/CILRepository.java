package com.collecte.projetCIL.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.collecte.projetCIL.models.CIL;

@Repository
public interface CILRepository extends JpaRepository<CIL, Long> {
    Optional<CIL> findByEmail(String email);
}