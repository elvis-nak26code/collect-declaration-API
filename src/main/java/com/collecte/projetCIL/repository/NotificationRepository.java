package com.collecte.projetCIL.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.collecte.projetCIL.models.Notification;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    /** Toutes les notifications d'un utilisateur, ordre anti-chronologique. */
    @Query("SELECT n FROM Notification n WHERE n.utilisateur.id = :utilisateurId ORDER BY n.dateEnvoi DESC")
    List<Notification> findByUtilisateurIdOrderByDateEnvoiDesc(@Param("utilisateurId") Long utilisateurId);

    /** Notifications non lues d'un utilisateur. */
    @Query("SELECT n FROM Notification n WHERE n.utilisateur.id = :utilisateurId AND n.statut = 'NON_LUE' ORDER BY n.dateEnvoi DESC")
    List<Notification> findNonLuesByUtilisateurId(@Param("utilisateurId") Long utilisateurId);
}