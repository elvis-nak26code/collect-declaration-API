package com.collecte.projetCIL.service;

import com.collecte.projetCIL.dto.request.SessionCollecteRequest;
import com.collecte.projetCIL.dto.response.SessionCollecteResponse;
import com.collecte.projetCIL.enums.StatutSession;
import com.collecte.projetCIL.models.DPO;
import com.collecte.projetCIL.models.SessionCollecte;
import com.collecte.projetCIL.repository.DPORepository;
import com.collecte.projetCIL.repository.SessionCollecteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SessionCollecteService {

    private final SessionCollecteRepository sessionCollecteRepository;
    private final DPORepository dpoRepository;

    // ------------------------------------------------------------------ //
    //  Créer une session de collecte
    // ------------------------------------------------------------------ //
    public SessionCollecteResponse creerSession(SessionCollecteRequest request) {

        // Récupérer l'email du DPO connecté depuis le token JWT
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        DPO dpo = dpoRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("DPO introuvable : " + email));

        SessionCollecte session = new SessionCollecte();
        session.setNomSession(request.getNomSession());
        session.setDateDebut(LocalDateTime.now());  // date de création automatique
        session.setDateFin(null);                    // sera renseignée à la clôture
        session.setTypeCollecte(request.getTypeCollecte());
        session.setLieu(request.getLieu());
        session.setDescription(request.getDescription());
        session.setStatutSession(StatutSession.EN_COURS);
        session.setDpo(dpo);

        SessionCollecte saved = sessionCollecteRepository.save(session);
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
    //  Mettre à jour le statut d'une session (EN_COURS -> TERMINEE / ANNULEE)
    //  Si le nouveau statut est TERMINEE, on enregistre la date de fin
    // ------------------------------------------------------------------ //
    public SessionCollecteResponse changerStatut(Long id, StatutSession nouveauStatut) {
        SessionCollecte session = sessionCollecteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Session introuvable avec l'id : " + id));

        session.setStatutSession(nouveauStatut);

        if (nouveauStatut == StatutSession.TERMINEE) {
            session.setDateFin(LocalDateTime.now()); // date de clôture automatique
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