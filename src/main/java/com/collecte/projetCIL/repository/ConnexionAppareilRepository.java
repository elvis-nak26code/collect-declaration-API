package com.collecte.projetCIL.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.collecte.projetCIL.models.ConnexionAppareil;

@Repository
public interface ConnexionAppareilRepository extends JpaRepository<ConnexionAppareil, Long> {

    Optional<ConnexionAppareil> findByEmailAndIpAddressAndUserAgent(
            String email, String ipAddress, String userAgent);

    List<ConnexionAppareil> findByEmail(String email);
}
