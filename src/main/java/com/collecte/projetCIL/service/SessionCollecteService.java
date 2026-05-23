package com.collecte.projetCIL.service;

import com.collecte.projetCIL.dto.request.SessionCollecteRequest;
import com.collecte.projetCIL.dto.response.SessionCollecteResponse;
import com.collecte.projetCIL.enums.StatutSession;
import com.collecte.projetCIL.models.DPO;
import com.collecte.projetCIL.models.SessionCollecte;
import com.collecte.projetCIL.repository.DPORepository;
import com.collecte.projetCIL.repository.SessionCollecteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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

        DPO dpo = dpoRepository.findById(request.getDpoId())
                .orElseThrow(() -> new RuntimeException("DPO introuvable avec l'id : " + request.getDpoId()));

        SessionCollecte session = new SessionCollecte();
        session.setDateDebut(request.getDateDebut());
        session.setDateFin(request.getDateFin());
        session.setTypeCollecte(request.getTypeCollecte());
        session.setLieu(request.getLieu());
        session.setDescription(request.getDescription());
        session.setStatutSession(StatutSession.EN_COURS);   // statut initial automatique
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
    // ------------------------------------------------------------------ //
    public SessionCollecteResponse changerStatut(Long id, StatutSession nouveauStatut) {
        SessionCollecte session = sessionCollecteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Session introuvable avec l'id : " + id));
        session.setStatutSession(nouveauStatut);
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
