package com.clinica.model;

import java.util.List;

/** Herança: Gato herda os atributos e métodos de Animal e acrescenta o temperamento. */
public class Gato extends Animal {

    public static final String ESPECIE = "gato";
    public static final List<String> TEMPERAMENTOS = List.of("Dócil", "Arredio", "Brincalhão");

    private String temperamento;

    public Gato(String nome, int idade, Tutor tutor, String temperamento) {
        // super chama o construtor da classe pai (Animal)
        super(nome, idade, tutor);
        this.temperamento = temperamento;
    }

    public String getTemperamento() {
        return temperamento;
    }

    public void setTemperamento(String temperamento) {
        this.temperamento = temperamento;
    }

    @Override
    public String getEspecie() {
        return ESPECIE;
    }

    @Override
    public String getTipo() {
        return "Gato";
    }

    @Override
    public String getCaracteristica() {
        return "Temperamento: " + temperamento;
    }

    @Override
    public String emitirSom() {
        return "Miau!";
    }
}
