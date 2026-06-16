package com.collecte.projetCIL.dto.request;

import lombok.Data;

@Data
public class DemandeRequest {

    /** ID de l'usager qui fait la demande. */
    private Long usagerId;

    /** ID de la donnée personnelle concernée. */
    private Long donneeId;

    /** "MODIFICATION" ou "SUPPRESSION". */
    private String typeDemande;

    /** Description / justification de la demande. */
    private String descriptionDemande;

    /** Nouvelle valeur souhaitée (uniquement pour MODIFICATION). */
    private String nouvelleValeur;
}
