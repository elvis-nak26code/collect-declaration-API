package com.collecte.projetCIL.dto.response;

import java.time.LocalDateTime;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CleApiCilResponse {
    private Long id;
    private String prefixeAffichage; // jamais la clé complète
    private String libelle;
    private boolean actif;
    private LocalDateTime dateCreation;
    private LocalDateTime derniereUtilisation;
}