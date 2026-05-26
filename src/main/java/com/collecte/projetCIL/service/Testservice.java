package com.collecte.projetCIL.service;

import com.collecte.projetCIL.models.teste;
import com.collecte.projetCIL.repository.TesteRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class Testservice {

    @Autowired
    private TesteRepository testeRepository;

    public teste ajouter(teste t) {
        return testeRepository.save(Objects.requireNonNull(t));
    }

    public List<teste> afficher() {
        return testeRepository.findAll();
    }
}