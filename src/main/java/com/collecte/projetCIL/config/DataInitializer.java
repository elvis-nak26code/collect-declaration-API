package com.collecte.projetCIL.config;

import com.collecte.projetCIL.models.Personne;
import com.collecte.projetCIL.models.Usager;
import com.collecte.projetCIL.repository.PersonneRepository;
import com.collecte.projetCIL.repository.UsagerRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.collecte.projetCIL.models.Administrateur;
import com.collecte.projetCIL.repository.AdministrateurRepository;

import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final AdministrateurRepository administrateurRepository;
    private final PasswordEncoder passwordEncoder;
    private final UsagerRepository usagerRepository;
    private final PersonneRepository personneRepository;

    @Override
    public void run(String... args) {
        if (!administrateurRepository.existsByEmail("admin@cil.com")) {
            Administrateur admin = new Administrateur();
            admin.setNom("Administrateur");
            admin.setPrenom("Système");
            admin.setEmail("admin@cil.com");
            admin.setMotDePasse(passwordEncoder.encode("Admin@1234"));
            admin.setFonction("Administrateur Système");
            administrateurRepository.save(admin);
            System.out.println("Compte admin créé : admin@cil.com / Admin@1234");
        }

        migrerUsagersVersPersonnes();
    }

    private void migrerUsagersVersPersonnes() {
        List<Usager> usagers = usagerRepository.findAll();
        for (Usager usager : usagers) {
            if (usager.getPersonne() != null) continue;

            Personne personne = null;

            if (usager.getEmail() != null && !usager.getEmail().isBlank()) {
                personne = personneRepository.findByEmail(usager.getEmail()).orElse(null);
            }
            if (personne == null && usager.getTelephone() != null && !usager.getTelephone().isBlank()) {
                personne = personneRepository.findByTelephone(usager.getTelephone()).orElse(null);
            }
            if (personne == null) {
                personne = new Personne();
                personne.setNom(usager.getNom());
                personne.setPrenom(usager.getPrenom());
                personne.setEmail(usager.getEmail());
                personne.setTelephone(usager.getTelephone());
                personne.setDateCreation(LocalDateTime.now());
                personne.setDateModification(LocalDateTime.now());
                personneRepository.save(personne);
            }

            usager.setPersonne(personne);
            usagerRepository.save(usager);
        }
        if (!usagers.isEmpty()) {
            System.out.println("Migration Personne effectuée pour " + usagers.size() + " usager(s).");
        }
    }
}
