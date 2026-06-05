package com.collecte.projetCIL.service;

import com.collecte.projetCIL.dto.response.JournalAuditResponse;
import com.collecte.projetCIL.enums.ModuleConserne;
import com.collecte.projetCIL.enums.ResultatAction;
import com.collecte.projetCIL.enums.TypeAction;
import com.collecte.projetCIL.models.JournalAudit;
import com.collecte.projetCIL.models.Utilisateur;
import com.collecte.projetCIL.repository.JournalAuditRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class JournalAuditService {

    private final JournalAuditRepository journalAuditRepository;

    // ------------------------------------------------------------------ //
    //  Enregistrer une action dans le journal (appelé depuis les services)
    // ------------------------------------------------------------------ //
    public void enregistrer(Utilisateur utilisateur,
                            TypeAction typeAction,
                            ModuleConserne module,
                            ResultatAction resultat) {
        JournalAudit journal = new JournalAudit();
        journal.setDateAction(LocalDate.now());
        journal.setTypeAction(typeAction);
        journal.setModuleConserne(module);
        journal.setResultatAction(resultat);
        journal.setUtilisateur(utilisateur);
        journalAuditRepository.save(journal);
    }

    // ------------------------------------------------------------------ //
    //  Lire tous les journaux — route admin, ordre anti-chronologique
    // ------------------------------------------------------------------ //
    public List<JournalAuditResponse> listerTousTriesParDate() {
        return journalAuditRepository.findAllOrderByDateActionDesc()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // ------------------------------------------------------------------ //
    //  Journaux d'un utilisateur précis
    // ------------------------------------------------------------------ //
    public List<JournalAuditResponse> listerParUtilisateur(Long utilisateurId) {
        return journalAuditRepository.findByUtilisateurIdOrderByDateActionDesc(utilisateurId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // ------------------------------------------------------------------ //
    //  Mapper
    // ------------------------------------------------------------------ //
    private JournalAuditResponse toResponse(JournalAudit j) {
        String nomPrenom = null;
        String email     = null;
        String role      = null;

        if (j.getUtilisateur() != null) {
            Utilisateur u = j.getUtilisateur();
            nomPrenom = u.getPrenom() + " " + u.getNom();
            email     = u.getEmail();
            role      = u.getPermission() != null ? u.getPermission().name() : null;
        }

        return new JournalAuditResponse(
                j.getIdJournal(),
                j.getDateAction(),
                j.getTypeAction(),
                j.getModuleConserne(),
                j.getResultatAction(),
                j.getUtilisateur() != null ? j.getUtilisateur().getId() : null,
                nomPrenom,
                email,
                role
        );
    }
}
