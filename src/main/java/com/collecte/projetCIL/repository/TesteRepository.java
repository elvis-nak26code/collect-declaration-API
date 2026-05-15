package com.collecte.projetCIL.repository;

import com.collecte.projetCIL.models.teste;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TesteRepository extends JpaRepository<teste, Long> {

}