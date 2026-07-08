package com.collecte.projetCIL.dto.request;

import lombok.Data;

/**
 * Requête de saisie manuelle d'une donnée directement dans l'entrepôt
 * (pas de traitement associé — même principe que l'import Excel entrepôt).
 */
@Data
public class EntrepotSaisieRequest {

    private String nom;
    private String prenom;
    private String email;
    private String telephone;

    private Long typeDonneeId;
    private String valeur;
}