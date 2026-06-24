package com.collecte.projetCIL.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.collecte.projetCIL.models.Usager;
import com.collecte.projetCIL.models.Utilisateur;

@Repository
public interface UsagerRepository extends JpaRepository<Usager, Long> {
    Optional<Utilisateur> findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByTelephone(String telephone);

    @Query("SELECT u FROM Usager u WHERE u.email = :email")
    Optional<Usager> findUsagerByEmail(@Param("email") String email);
}