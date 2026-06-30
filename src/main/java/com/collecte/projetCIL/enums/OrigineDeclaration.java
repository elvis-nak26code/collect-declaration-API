package com.collecte.projetCIL.enums;

/**
 * Distingue les déclarations créées automatiquement (en BROUILLON, lors de la
 * création d'un traitement par l'Utilisateur Métier) de celles créées
 * manuellement par le DPO via le tableau de bord.
 */
public enum OrigineDeclaration {
    AUTOMATIQUE, // créée automatiquement avec le traitement, statut BROUILLON, invisible pour le DPO tant qu'il ne la soumet pas
    MANUELLE     // créée explicitement par le DPO via "Déclarer"
}