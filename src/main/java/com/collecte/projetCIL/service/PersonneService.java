package com.collecte.projetCIL.service;

import com.collecte.projetCIL.dto.request.PersonneRequest;
import com.collecte.projetCIL.dto.response.PersonneResponse;
import com.collecte.projetCIL.models.Personne;
import com.collecte.projetCIL.repository.PersonneRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PersonneService {

    private final PersonneRepository personneRepository;

    public Optional<Personne> findByEmail(String email) {
        return personneRepository.findByEmail(email);
    }

    public Optional<Personne> findByTelephone(String telephone) {
        return personneRepository.findByTelephone(telephone);
    }

    public Personne creerPersonne(String nom, String prenom, String email, String telephone) {
        Personne p = new Personne();
        p.setNom(nom);
        p.setPrenom(prenom);
        p.setEmail(email);
        p.setTelephone(telephone);
        p.setDateCreation(LocalDateTime.now());
        p.setDateModification(LocalDateTime.now());
        return personneRepository.save(p);
    }

    public Personne creerOuRecuperer(String nom, String prenom, String email, String telephone) {
        if (email != null && !email.isBlank()) {
            Optional<Personne> existant = findByEmail(email);
            if (existant.isPresent()) return existant.get();
        }
        if (telephone != null && !telephone.isBlank()) {
            Optional<Personne> existant = findByTelephone(telephone);
            if (existant.isPresent()) return existant.get();
        }
        return creerPersonne(nom, prenom, email, telephone);
    }

    /**
     * Recherche utilisée par le front (autocomplétion). Sans paramètre, renvoie
     * une liste bornée des dernières personnes connues pour éviter de tout charger.
     */
    public List<PersonneResponse> rechercher(String q) {
        List<Personne> resultats;
        if (q == null || q.isBlank()) {
            resultats = personneRepository.findAll().stream().limit(50).collect(Collectors.toList());
        } else {
            resultats = personneRepository
                    .findByNomContainingIgnoreCaseOrPrenomContainingIgnoreCaseOrEmailContainingIgnoreCaseOrTelephoneContainingIgnoreCase(
                            q, q, q, q);
        }
        return resultats.stream().map(this::toResponse).collect(Collectors.toList());
    }

    /**
     * Création depuis le front : dédoublonne automatiquement sur email/téléphone
     * pour éviter de créer deux fiches pour la même personne.
     */
    public PersonneResponse creerDepuisRequest(PersonneRequest request) {
        Personne p = creerOuRecuperer(request.getNom(), request.getPrenom(), request.getEmail(), request.getTelephone());
        if (request.getNumeroIdentite() != null && !request.getNumeroIdentite().isBlank()) {
            p.setNumeroIdentite(request.getNumeroIdentite());
        }
        p.setDateModification(LocalDateTime.now());
        p = personneRepository.save(p);
        return toResponse(p);
    }

    private PersonneResponse toResponse(Personne p) {
        String nomComplet = ((p.getPrenom() != null ? p.getPrenom() : "") + " " + (p.getNom() != null ? p.getNom() : "")).trim();
        return new PersonneResponse(p.getId(), p.getNom(), p.getPrenom(), nomComplet, p.getEmail(), p.getTelephone());
    }
}
