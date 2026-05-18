package com.collecte.projetCIL.dto.response;

import com.collecte.projetCIL.enums.StatutDemandeAcces;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DemandeAccesResponse {
    private Long id;
    private String nomUtilisateur;
    private String prenomUtilisateur;
    private String emailUtilisateur;
    private String typeUtilisateur;
    private StatutDemandeAcces statut;
    private LocalDateTime dateDemande;
    private LocalDateTime dateValidation;
    private String motif;
}
