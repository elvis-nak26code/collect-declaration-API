package com.collecte.projetCIL.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.collecte.projetCIL.models.JournalAudit;

@Repository
public interface JournalAuditRepository extends JpaRepository<JournalAudit, Long> {

    /** Tous les journaux triés du plus récent au plus ancien (pour l'admin). */
    @Query("SELECT j FROM JournalAudit j ORDER BY j.dateAction DESC")
    List<JournalAudit> findAllOrderByDateActionDesc();

    /** Journaux d'un utilisateur précis, ordre anti-chronologique. */
    @Query("SELECT j FROM JournalAudit j WHERE j.utilisateur.id = :utilisateurId ORDER BY j.dateAction DESC")
    List<JournalAudit> findByUtilisateurIdOrderByDateActionDesc(@Param("utilisateurId") Long utilisateurId);
}