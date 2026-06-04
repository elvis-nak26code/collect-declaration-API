package com.collecte.projetCIL.security;

import java.util.List;
import java.util.Optional;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.collecte.projetCIL.models.Administrateur;
import com.collecte.projetCIL.models.Utilisateur;
import com.collecte.projetCIL.repository.AdministrateurRepository;
import com.collecte.projetCIL.repository.CILRepository;
import com.collecte.projetCIL.repository.DPORepository;
import com.collecte.projetCIL.repository.DGRepository;
import com.collecte.projetCIL.repository.UsagerRepository;
import com.collecte.projetCIL.repository.UtilisateurRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UtilisateurRepository    utilisateurRepository;
    private final AdministrateurRepository administrateurRepository;
    private final DPORepository            dpoRepository;
    private final CILRepository            cilRepository;
    private final DGRepository             dgRepository;
    private final UsagerRepository         usagerRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        // 1. Administrateur
        Optional<Administrateur> admin = administrateurRepository.findByEmail(email);
        if (admin.isPresent()) {
            Administrateur a = admin.get();
            return new User(a.getEmail(), a.getMotDePasse(),
                    List.of(new SimpleGrantedAuthority("ROLE_ADMINISTRATEUR")));
        }

        // 2. DPO
        if (dpoRepository.findByEmail(email).isPresent()) {
            Utilisateur u = utilisateurRepository.findByEmail(email)
                    .orElseThrow(() -> new UsernameNotFoundException("Introuvable : " + email));
            return new User(u.getEmail(), u.getMotDePasse(),
                    List.of(new SimpleGrantedAuthority("ROLE_DPO")));
        }

        // 3. CIL
        if (cilRepository.findByEmail(email).isPresent()) {
            Utilisateur u = utilisateurRepository.findByEmail(email)
                    .orElseThrow(() -> new UsernameNotFoundException("Introuvable : " + email));
            return new User(u.getEmail(), u.getMotDePasse(),
                    List.of(new SimpleGrantedAuthority("ROLE_CIL")));
        }

        // 4. DG
        if (dgRepository.findByEmail(email).isPresent()) {
            Utilisateur u = utilisateurRepository.findByEmail(email)
                    .orElseThrow(() -> new UsernameNotFoundException("Introuvable : " + email));
            return new User(u.getEmail(), u.getMotDePasse(),
                    List.of(new SimpleGrantedAuthority("ROLE_DG")));
        }

        // 5. Usager
        if (usagerRepository.findByEmail(email).isPresent()) {
            Utilisateur u = utilisateurRepository.findByEmail(email)
                    .orElseThrow(() -> new UsernameNotFoundException("Introuvable : " + email));
            return new User(u.getEmail(), u.getMotDePasse(),
                    List.of(new SimpleGrantedAuthority("ROLE_USAGER")));
        }

        throw new UsernameNotFoundException("Utilisateur non trouvé : " + email);
    }
}