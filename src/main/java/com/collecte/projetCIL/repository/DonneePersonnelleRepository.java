package com.collecte.projetCIL.repository;

import com.collecte.projetCIL.models.DonneePersonnelle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DonneePersonnelleRepository extends JpaRepository<DonneePersonnelle, Long> {
}
