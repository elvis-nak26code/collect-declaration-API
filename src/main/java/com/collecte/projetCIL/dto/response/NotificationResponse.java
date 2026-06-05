package com.collecte.projetCIL.dto.response;

import com.collecte.projetCIL.enums.StatutNotification;
import com.collecte.projetCIL.enums.TypeNotification;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {

    private Long idNotification;
    private LocalDate dateEnvoi;
    private TypeNotification typeNotification;
    private String contenu;
    private StatutNotification statut;
    private LocalDate dateEcheance;

    // Destinataire
    private Long utilisateurId;
    private String utilisateurNomPrenom;
}
