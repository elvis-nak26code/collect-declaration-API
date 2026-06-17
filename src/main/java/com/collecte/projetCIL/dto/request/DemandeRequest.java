package com.collecte.projetCIL.dto.request;

import lombok.Data;

@Data
public class DemandeRequest {

    private Long usagerId;
    private Long personneId;
    private Long donneeId;
    private String typeDemande;
    private String descriptionDemande;
    private String nouvelleValeur;
}
