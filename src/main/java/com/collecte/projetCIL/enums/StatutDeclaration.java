package com.collecte.projetCIL.enums;

public enum StatutDeclaration {
    BROUILLON,           // créée mais pas encore soumise au DG
    EN_ATTENTE,          // soumise au DG, en attente de validation
    APPROUVEE_DG,        // validée par le DG, transmise à la CIL
    REJETEE_DG,          // rejetée par le DG, DPO doit corriger
    EN_VERIFICATION_CIL, // reçue par la CIL pour vérification de conformité
    VALIDEE_CIL,         // validée conforme par la CIL
    REJETEE_CIL,         // non conforme selon la CIL
    APPROUVEE,           // alias légacy / état final accepté
    REJETEE              // alias légacy / état final rejeté
}
