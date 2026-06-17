package com.collecte.projetCIL.service;

import com.collecte.projetCIL.models.Personne;
import com.collecte.projetCIL.repository.PersonneRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

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
}
