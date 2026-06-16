package com.collecte.projetCIL.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DonneePersonnelleResponse {

    private Long idDonnee;
    private String valeur;
    private LocalDateTime dateCollecte;

    // Usager concerné
    private Long usagerId;
    private String usagerNomComplet;

    // Type de donnée
    private Long typeDonneeId;
    private String typeDonneeNom;
    private Boolean typeDonneeSensible;

    // Traitement source (collecte)
    private Long traitementId;
    private String traitementNom;
}
