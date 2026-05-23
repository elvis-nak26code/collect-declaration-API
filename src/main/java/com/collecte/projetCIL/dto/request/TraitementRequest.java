package com.collecte.projetCIL.dto.request;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TraitementRequest {

    private String department;
    private String description;
    private String texte;
    private String certificationSecurite;
    private Integer dureeConservation;     // en mois
    private LocalDateTime dateFin;
    private Long utilisateurMetierId;      // ID de l'UtilisateurMetier qui gère ce traitement
    private Long sessionCollecteId;        // ID de la session à laquelle ce traitement est rattaché
}
