package com.collecte.projetCIL.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.collecte.projetCIL.models.DG;

@Repository
public interface DGRepository extends JpaRepository<DG, Long> {
    Optional<DG> findByEmail(String email);
}