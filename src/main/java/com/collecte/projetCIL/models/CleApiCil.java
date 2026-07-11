package com.collecte.projetCIL.models;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Clé API permettant à un système externe de la CIL d'appeler
 * /api/cil-externe/** SANS login/JWT et SANS aucune fiche CIL en base :
 * cette entité est totalement autonome, elle ne dépend d'aucun autre
 * modèle (ni CIL, ni Utilisateur).
 *
 * On ne stocke jamais la clé en clair, uniquement son empreinte SHA-256
 * (cleHachee) — comme un mot de passe. La clé en clair n'est communiquée
 * qu'une seule fois, à la génération (voir CleApiCilService).
 */
@Entity
@Table(name = "cle_api_cil")
@Data
@NoArgsConstructor
public class CleApiCil {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Empreinte SHA-256 de la clé en clair (jamais la clé elle-même). */
    private String cleHachee;

    /** Les premiers caractères de la clé en clair, pour identification en admin (ex: "cil_live_8f3a1c9e..."). Pas un secret. */
    private String prefixeAffichage;

    /** Nom libre du partenaire/instance (ex: "Système interne CIL - prod"). */
    private String libelle;

    private boolean actif = true;

    private LocalDateTime dateCreation;

    private LocalDateTime derniereUtilisation;
}
