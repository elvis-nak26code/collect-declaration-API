// src/main/java/com/collecte/projetCIL/service/AdminProfilService.java
package com.collecte.projetCIL.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.collecte.projetCIL.dto.request.AdminMotDePasseRequest;
import com.collecte.projetCIL.dto.request.AdminProfilRequest;
import com.collecte.projetCIL.dto.response.MessageResponse;
import com.collecte.projetCIL.models.Administrateur;
import com.collecte.projetCIL.repository.AdministrateurRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminProfilService {

    private final AdministrateurRepository administrateurRepository;
    private final PasswordEncoder passwordEncoder;

    public MessageResponse modifierProfil(String email, AdminProfilRequest req) {
        if (req.getNom() == null || req.getNom().isBlank())
            throw new RuntimeException("Le nom est obligatoire.");
        if (req.getPrenom() == null || req.getPrenom().isBlank())
            throw new RuntimeException("Le prénom est obligatoire.");

        Administrateur admin = administrateurRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Administrateur introuvable : " + email));

        admin.setNom(req.getNom().trim());
        admin.setPrenom(req.getPrenom().trim());
        administrateurRepository.save(admin);

        return new MessageResponse("Profil mis à jour avec succès.");
    }

    public MessageResponse changerMotDePasse(String email, AdminMotDePasseRequest req) {
        if (req.getNouveauMotDePasse() == null || req.getNouveauMotDePasse().length() < 6)
            throw new RuntimeException("Le nouveau mot de passe doit faire au moins 6 caractères.");

        Administrateur admin = administrateurRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Administrateur introuvable : " + email));

        if (!passwordEncoder.matches(req.getAncienMotDePasse(), admin.getMotDePasse()))
            throw new RuntimeException("Ancien mot de passe incorrect.");

        admin.setMotDePasse(passwordEncoder.encode(req.getNouveauMotDePasse()));
        administrateurRepository.save(admin);

        return new MessageResponse("Mot de passe modifié avec succès.");
    }
}