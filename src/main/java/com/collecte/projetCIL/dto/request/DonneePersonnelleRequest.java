package com.collecte.projetCIL.dto.request;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DonneePersonnelleRequest {

    private String valeur;
    private LocalDateTime dateCollecte;
    private Long usagerId;       // ID de l'Usager concerné
    private Long typeDonneeId;   // ID du TypeDonnee
    private Long traitementId;   // ID du Traitement auquel cette donnée est rattachée
}
