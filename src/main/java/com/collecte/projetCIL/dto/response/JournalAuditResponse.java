package com.collecte.projetCIL.dto.response;

import com.collecte.projetCIL.enums.ModuleConserne;
import com.collecte.projetCIL.enums.ResultatAction;
import com.collecte.projetCIL.enums.TypeAction;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JournalAuditResponse {

    private Long idJournal;
    private LocalDate dateAction;
    private TypeAction typeAction;
    private ModuleConserne moduleConserne;
    private ResultatAction resultatAction;

    // Infos de l'utilisateur concerné
    private Long utilisateurId;
    private String utilisateurNomPrenom;
    private String utilisateurEmail;
    private String utilisateurRole;   // ex. "DPO", "DG", "UTILISATEUR_METIER"
}
