package com.collecte.projetCIL.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
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
import com.collecte.projetCIL.repository.TraitementRepository;
import com.collecte.projetCIL.repository.UtilisateurMetierRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SessionCollecteService {

    private final SessionCollecteRepository   sessionCollecteRepository;
    private final DPORepository               dpoRepository;
    private final UtilisateurMetierRepository utilisateurMetierRepository;
    private final NotificationService         notificationService;
    private final TraitementRepository        traitementRepository;

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
        // 1 requête : nombre de traitements par session (au lieu d'1 par session)
        Map<Long, Long> nbTraitementsParSession = traitementRepository.countTraitementsParSession()
                .stream()
                .collect(Collectors.toMap(
                        row -> (Long) row[0],
                        row -> (Long) row[1]
                ));

        // 1 requête : toutes les sessions + leur DPO (LEFT JOIN FETCH, au lieu
        // d'1 requête DPO par session)
        return sessionCollecteRepository.findAllWithDpo()
                .stream()
                .map(s -> toResponse(s, nbTraitementsParSession.getOrDefault(s.getIdSession(), 0L).intValue()))
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
    //  Modifier une session existante
    // ------------------------------------------------------------------ //
    @Transactional
    public SessionCollecteResponse modifierSession(Long id, SessionCollecteRequest request) {
        SessionCollecte session = sessionCollecteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Session introuvable avec l'id : " + id));

        if (request.getNomSession() != null)   session.setNomSession(request.getNomSession());
        if (request.getLieu() != null)         session.setLieu(request.getLieu());
        if (request.getTypeCollecte() != null) session.setTypeCollecte(request.getTypeCollecte());
        if (request.getDescription() != null)  session.setDescription(request.getDescription());
        if (request.getDateDebut() != null)     session.setDateDebut(request.getDateDebut());
        if (request.getDateFin() != null)       session.setDateFin(request.getDateFin());

        SessionCollecte saved = sessionCollecteRepository.save(session);
        return toResponse(saved);
    }

    // ------------------------------------------------------------------ //
    //  Mapper entité -> DTO réponse
    // ------------------------------------------------------------------ //
    private SessionCollecteResponse toResponse(SessionCollecte s) {
        int nbTraitements = (s.getTraitements() != null) ? s.getTraitements().size() : 0;
        return toResponse(s, nbTraitements);
    }

    private SessionCollecteResponse toResponse(SessionCollecte s, int nbTraitements) {
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