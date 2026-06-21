package com.collecte.projetCIL.service;

import com.collecte.projetCIL.dto.request.LoginRequest;
import com.collecte.projetCIL.dto.response.AuthResponse;
import com.collecte.projetCIL.models.Administrateur;
import com.collecte.projetCIL.models.Utilisateur;
import com.collecte.projetCIL.repository.AdministrateurRepository;
import com.collecte.projetCIL.repository.UtilisateurRepository;
import com.collecte.projetCIL.security.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final UtilisateurRepository utilisateurRepository;
    private final AdministrateurRepository administrateurRepository;

    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(), request.getMotDePasse())
        );
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String email = userDetails.getUsername();
        String role = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst()
                .orElse("ROLE_UNKNOWN");

        // UserDetails ne porte que l'email + les rôles : on va chercher
        // l'id / nom / prénom réels selon le type de compte, pour pouvoir
        // les embarquer dans le JWT (le frontend en a besoin).
        Long id;
        String nom;
        String prenom;

        if ("ROLE_ADMINISTRATEUR".equals(role)) {
            Administrateur admin = administrateurRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Administrateur introuvable : " + email));
            id = admin.getId();
            nom = admin.getNom();
            prenom = admin.getPrenom();
        } else {
            Utilisateur u = utilisateurRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Utilisateur introuvable : " + email));
            id = u.getId();
            nom = u.getNom();
            prenom = u.getPrenom();
        }

        String token = jwtUtils.generateToken(email, role, id, nom, prenom);
        return new AuthResponse(token, email, role, id);
    }
}