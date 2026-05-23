package com.collecte.projetCIL.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImportResultResponse {

    private int totalLignes;
    private int lignesImportees;
    private int lignesEchouees;
    private List<String> erreurs;   // ex: "Ligne 3 : usagerId introuvable"
}
