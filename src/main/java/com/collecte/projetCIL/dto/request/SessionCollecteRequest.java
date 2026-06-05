package com.collecte.projetCIL.dto.request;

import com.collecte.projetCIL.enums.TypeCollecte;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SessionCollecteRequest {
    private String nomSession;
    private LocalDateTime dateDebut;
    private LocalDateTime dateFin;
    private TypeCollecte typeCollecte;   // EN_LIGNE | TERRAIN
    private String lieu;
    private String description;
    private Long dpoId;                  // ID du DPO qui initie la session
}
