package com.collecte.projetCIL.security;

import com.collecte.projetCIL.enums.StatutUtilisateur;
import com.collecte.projetCIL.models.Administrateur;
import com.collecte.projetCIL.models.Utilisateur;
import com.collecte.projetCIL.repository.AdministrateurRepository;
import com.collecte.projetCIL.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UtilisateurRepository utilisateurRepository;
    private final AdministrateurRepository administrateurRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        // Chercher dans Utilisateur
        Optional<Utilisateur> utilisateur = utilisateurRepository.findByEmail(email);
        if (utilisateur.isPresent()) {
            Utilisateur u = utilisateur.get();

            // Bloquer si compte pas encore validé
            if (u.getStatutUtilisateur() != StatutUtilisateur.ACTIF) {
                throw new UsernameNotFoundException("Compte non activé. Veuillez attendre la validation de l'administrateur.");
            }

            String role = "ROLE_" + u.getClass().getSimpleName().toUpperCase();
            return new User(u.getEmail(), u.getMotDePasse(),
                    List.of(new SimpleGrantedAuthority(role)));
        }

        // Chercher dans Administrateur
        Optional<Administrateur> admin = administrateurRepository.findByEmail(email);
        if (admin.isPresent()) {
            Administrateur a = admin.get();
            return new User(a.getEmail(), a.getMotDePasse(),
                    List.of(new SimpleGrantedAuthority("ROLE_ADMINISTRATEUR")));
        }

        throw new UsernameNotFoundException("Utilisateur non trouvé : " + email);
    }
}
