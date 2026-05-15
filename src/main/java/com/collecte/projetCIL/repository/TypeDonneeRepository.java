package com.collecte.projetCIL.repository;

import com.collecte.projetCIL.models.TypeDonnee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TypeDonneeRepository extends JpaRepository<TypeDonnee, Long> {
}
