package com.collecte.projetCIL.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.collecte.projetCIL.dto.request.SessionCollecteRequest;
import com.collecte.projetCIL.dto.response.SessionCollecteResponse;
import com.collecte.projetCIL.enums.StatutSession;
import com.collecte.projetCIL.enums.TypeNotification;
import com.collecte.projetCIL.models.DPO;
import com.collecte.projetCIL.models.SessionCollecte;
import com.collecte.projetCIL.models.UtilisateurMetier;
import com.collecte.projetCIL.repository.DPORepository;
import com.collecte.projetCIL.repository.SessionCollecteRepository;
import com.collecte.projetCIL.repository.UtilisateurMetierRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SessionCollecteService {

    private final SessionCollecteRepository   sessionCollecteRepository;
    private final DPORepository               dpoRepository;
    private final UtilisateurMetierRepository utilisateurMetierRepository;
    private final NotificationService         notificationService;

    // ------------------------------------------------------------------ //
    //  Créer une session de collecte
    //  → Notifie tous les UtilisateurMetier actifs de la nouvelle session
    // ------------------------------------------------------------------ //
    @Transactional
    public SessionCollecteResponse creerSession(SessionCollecteRequest request) {

        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        DPO dpo = dpoRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("DPO introuvable : " + email));

        SessionCollecte session = new SessionCollecte();
        session.setNomSession(request.getNomSession());
        session.setDateDebut(request.getDateDebut() != null ? request.getDateDebut() : LocalDateTime.now());
        session.setDateFin(request.getDateFin());
        session.setTypeCollecte(request.getTypeCollecte());
        session.setLieu(request.getLieu());
        session.setDescription(request.getDescription());
        session.setStatutSession(StatutSession.EN_COURS);
        session.setDpo(dpo);

        SessionCollecte saved = sessionCollecteRepository.save(session);

        // Notifier tous les UtilisateurMetier de la nouvelle session
        List<UtilisateurMetier> metiers = utilisateurMetierRepository.findAll();
        String message = "Une nouvelle session de collecte « " + saved.getNomSession() +
                         " » a été ouverte par " + dpo.getPrenom() + " " + dpo.getNom() + ".";
        metiers.forEach(um -> notificationService.envoyer(um, TypeNotification.ALERTE, message));

        return toResponse(saved);
    }

    // ------------------------------------------------------------------ //
    //  Lister toutes les sessions
    // ------------------------------------------------------------------ //
    public List<SessionCollecteResponse> listerSessions() {
        return sessionCollecteRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // ------------------------------------------------------------------ //
    //  Obtenir une session par ID
    // ------------------------------------------------------------------ //
    public SessionCollecteResponse getSessionById(Long id) {
        SessionCollecte session = sessionCollecteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Session introuvable avec l'id : " + id));
        return toResponse(session);
    }

    // ------------------------------------------------------------------ //
    //  Mettre à jour le statut (EN_COURS -> TERMINEE / ANNULEE)
    // ------------------------------------------------------------------ //
    public SessionCollecteResponse changerStatut(Long id, StatutSession nouveauStatut) {
        SessionCollecte session = sessionCollecteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Session introuvable avec l'id : " + id));

        session.setStatutSession(nouveauStatut);

        if (nouveauStatut == StatutSession.TERMINEE) {
            session.setDateFin(LocalDateTime.now());
        }

        return toResponse(sessionCollecteRepository.save(session));
    }

    // ------------------------------------------------------------------ //
    //  Mapper entité -> DTO réponse
    // ------------------------------------------------------------------ //
    private SessionCollecteResponse toResponse(SessionCollecte s) {
        int nbTraitements = (s.getTraitements() != null) ? s.getTraitements().size() : 0;
        String nomDpo = (s.getDpo() != null)
                ? s.getDpo().getPrenom() + " " + s.getDpo().getNom()
                : null;
        return new SessionCollecteResponse(
            s.getIdSession(),
            s.getNomSession(),
            s.getDateDebut(),
            s.getDateFin(),
            s.getStatutSession(),
            s.getTypeCollecte(),
            s.getLieu(),
            s.getDescription(),
            s.getDpo() != null ? s.getDpo().getId() : null,
            nomDpo,
            nbTraitements
        );
    }
}