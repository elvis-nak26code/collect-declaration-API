package com.collecte.projetCIL.service;

import com.collecte.projetCIL.dto.request.TraitementRequest;
import com.collecte.projetCIL.dto.response.TraitementResponse;
import com.collecte.projetCIL.models.SessionCollecte;
import com.collecte.projetCIL.models.Traitement;
import com.collecte.projetCIL.models.UtilisateurMetier;
import com.collecte.projetCIL.repository.SessionCollecteRepository;
import com.collecte.projetCIL.repository.TraitementRepository;
import com.collecte.projetCIL.repository.UtilisateurMetierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TraitementService {

    private final TraitementRepository traitementRepository;
    private final SessionCollecteRepository sessionCollecteRepository;
    private final UtilisateurMetierRepository utilisateurMetierRepository;

    // ------------------------------------------------------------------ //
    //  Créer un traitement rattaché à une session de collecte
    // ------------------------------------------------------------------ //
    public TraitementResponse creerTraitement(TraitementRequest request) {

        SessionCollecte session = sessionCollecteRepository.findById(request.getSessionCollecteId())
                .orElseThrow(() -> new RuntimeException("Session introuvable avec l'id : " + request.getSessionCollecteId()));

        UtilisateurMetier utilisateurMetier = utilisateurMetierRepository.findById(request.getUtilisateurMetierId())
                .orElseThrow(() -> new RuntimeException("UtilisateurMetier introuvable avec l'id : " + request.getUtilisateurMetierId()));

        Traitement traitement = new Traitement();
        traitement.setDepartment(request.getDepartment());
        traitement.setDescription(request.getDescription());
        traitement.setTexte(request.getTexte());
        traitement.setCertificationSecurite(request.getCertificationSecurite());
        traitement.setDureeConservation(request.getDureeConservation());
        traitement.setDateCreation(LocalDateTime.now());
        traitement.setDateFin(request.getDateFin());
        traitement.setNombreDonnee(0L);                // initialisation à 0
        traitement.setSessionCollecte(session);
        traitement.setUtilisateurMetier(utilisateurMetier);

        Traitement saved = traitementRepository.save(traitement);
        return toResponse(saved);
    }

    // ------------------------------------------------------------------ //
    //  Lister les traitements d'une session
    // ------------------------------------------------------------------ //
    public List<TraitementResponse> listerParSession(Long sessionId) {
        return traitementRepository.findAll()
                .stream()
                .filter(t -> t.getSessionCollecte() != null
                        && t.getSessionCollecte().getIdSession().equals(sessionId))
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // ------------------------------------------------------------------ //
    //  Obtenir un traitement par ID
    // ------------------------------------------------------------------ //
    public TraitementResponse getTraitementById(Long id) {
        Traitement traitement = traitementRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Traitement introuvable avec l'id : " + id));
        return toResponse(traitement);
    }

    // ------------------------------------------------------------------ //
    //  Méthode interne : incrémenter le compteur de données d'un traitement
    //  (appelée par DonneePersonnelleService après chaque ajout)
    // ------------------------------------------------------------------ //
    public void incrementerNombreDonnee(Long traitementId, long quantite) {
        Traitement traitement = traitementRepository.findById(traitementId)
                .orElseThrow(() -> new RuntimeException("Traitement introuvable avec l'id : " + traitementId));
        long actuel = traitement.getNombreDonnee() != null ? traitement.getNombreDonnee() : 0L;
        traitement.setNombreDonnee(actuel + quantite);
        traitementRepository.save(traitement);
    }

    // ------------------------------------------------------------------ //
    //  Mapper entité -> DTO réponse
    // ------------------------------------------------------------------ //
    private TraitementResponse toResponse(Traitement t) {
        String nomMetier = (t.getUtilisateurMetier() != null)
                ? t.getUtilisateurMetier().getPrenom() + " " + t.getUtilisateurMetier().getNom()
                : null;
        return new TraitementResponse(
                t.getIdTraitement(),
                t.getDepartment(),
                t.getDescription(),
                t.getTexte(),
                t.getCertificationSecurite(),
                t.getDureeConservation(),
                t.getDateCreation(),
                t.getDateFin(),
                t.getNombreDonnee(),
                t.getSessionCollecte() != null ? t.getSessionCollecte().getIdSession() : null,
                t.getUtilisateurMetier() != null ? t.getUtilisateurMetier().getId() : null,
                nomMetier
        );
    }
}
