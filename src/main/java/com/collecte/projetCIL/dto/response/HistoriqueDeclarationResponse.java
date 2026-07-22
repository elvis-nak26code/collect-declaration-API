package com.collecte.projetCIL.dto.response;

import java.time.LocalDate;

import com.collecte.projetCIL.enums.StatutDeclaration;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class HistoriqueDeclarationResponse {

    private Long idHistorique;
    private LocalDate dateDeclaration;
    private String responsableDeclaration;
    private StatutDeclaration statut;

    private Long idDeclaration;
    private String typeDeclaration;
    private String intitule;          // dénomination / nom de traitement, pour l'affichage
    private String motifRejetCil;     // renseigné uniquement si statut == REJETEE_CIL

    private Long traitementId;
    private String traitementNom;
}