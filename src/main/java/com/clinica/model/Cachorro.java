package com.clinica.model;

import java.util.List;

/** Herança: Cachorro herda os atributos e métodos de Animal e acrescenta o porte. */
public class Cachorro extends Animal {

    public static final String ESPECIE = "cachorro";
    public static final List<String> PORTES = List.of("Pequeno", "Médio", "Grande");

    private String porte;

    public Cachorro(String nome, int idade, Tutor tutor, String porte) {
        // super chama o construtor da classe pai (Animal)
        super(nome, idade, tutor);
        this.porte = porte;
    }

    public String getPorte() {
        return porte;
    }

    public void setPorte(String porte) {
        this.porte = porte;
    }

    @Override
    public String getEspecie() {
        return ESPECIE;
    }

    @Override
    public String getTipo() {
        return "Cachorro";
    }

    @Override
    public String getCaracteristica() {
        return "Porte: " + porte;
    }

    @Override
    public String emitirSom() {
        return "Au Au!";
    }
}
