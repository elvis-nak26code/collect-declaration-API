package com.collecte.projetCIL.dto.response;

import com.collecte.projetCIL.enums.StatutSession;
import com.collecte.projetCIL.enums.TypeCollecte;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SessionCollecteResponse {

    private Long idSession;
    private LocalDateTime dateDebut;
    private LocalDateTime dateFin;
    private StatutSession statutSession;
    private TypeCollecte typeCollecte;
    private String lieu;
    private String description;
    private Long dpoId;
    private String dpoNomComplet;
    private int nombreTraitements;
}
