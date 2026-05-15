package com.collecte.projetCIL.repository;

import com.collecte.projetCIL.models.Usager;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UsagerRepository extends JpaRepository<Usager, Long> {
}
