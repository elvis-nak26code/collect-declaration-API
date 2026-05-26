package com.collecte.projetCIL.service;

import com.collecte.projetCIL.dto.response.UtilisateurResponse;
import com.collecte.projetCIL.models.Utilisateur;
import com.collecte.projetCIL.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UtilisateurService {

    private final UtilisateurRepository utilisateurRepository;

    public List<UtilisateurResponse> listerTous() {
        return utilisateurRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private UtilisateurResponse toResponse(Utilisateur u) {
        UtilisateurResponse r = new UtilisateurResponse();
        r.setId(u.getId());
        r.setNom(u.getNom());
        r.setPrenom(u.getPrenom());
        r.setEmail(u.getEmail());
        r.setStatutUtilisateur(u.getStatutUtilisateur());
        r.setTypeUtilisateur(u.getClass().getSimpleName());
        r.setDateCreation(u.getDateCreation());
        return r;
    }
}
