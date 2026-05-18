package com.collecte.projetCIL.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.collecte.projetCIL.models.Administrateur;
import com.collecte.projetCIL.repository.AdministrateurRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final AdministrateurRepository administrateurRepository;
    private final PasswordEncoder passwordEncoder;

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
    }
}
