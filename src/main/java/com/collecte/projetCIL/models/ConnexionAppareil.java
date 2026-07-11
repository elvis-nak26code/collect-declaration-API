package com.collecte.projetCIL.models;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Empreinte d'un appareil/navigateur déjà utilisé pour se connecter avec un
 * compte donné (email). Sert uniquement à détecter une NOUVELLE connexion
 * (nouvel appareil / nouvelle adresse IP) afin d'envoyer une alerte de
 * sécurité par email — ce n'est pas un journal d'audit complet (voir
 * JournalAuditService pour ça).
 */
@Entity
@Table(name = "connexion_appareil")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConnexionAppareil {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Email du compte (Utilisateur ou Administrateur), sert de clé de recherche. */
    private String email;

    private String ipAddress;

    private String userAgent;

    private LocalDateTime premiereConnexion;

    private LocalDateTime derniereConnexion;
}
