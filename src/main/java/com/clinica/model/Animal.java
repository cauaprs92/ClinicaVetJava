package com.clinica.model;

/**
 * Paciente da clínica. Tabela: animal.
 *
 * Abstração: Animal define o que todo paciente tem em comum, mas não pode ser
 * instanciada diretamente. Cada espécie (Cachorro, Gato) é uma subclasse com a
 * sua característica própria, como no app de terminal (console-app/).
 */
public abstract class Animal {

    // Encapsulamento: os atributos são private e só mudam pelos métodos get e set.
    private int id;
    private String nome;
    private int idade;
    private Tutor tutor;

    protected Animal(String nome, int idade, Tutor tutor) {
        this.nome = nome;
        this.idade = idade;
        this.tutor = tutor;
    }

    /**
     * Cria o animal da espécie certa. O retorno é do tipo Animal, mas o objeto é
     * um Cachorro ou um Gato (polimorfismo).
     *
     * @param caracteristica o porte (cachorro) ou o temperamento (gato)
     */
    public static Animal criar(String especie, String nome, int idade, String caracteristica, Tutor tutor) {
        return switch (especie) {
            case Cachorro.ESPECIE -> new Cachorro(nome, idade, tutor, caracteristica);
            case Gato.ESPECIE -> new Gato(nome, idade, tutor, caracteristica);
            default -> throw new IllegalArgumentException("Espécie desconhecida: " + especie);
        };
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public int getIdade() {
        return idade;
    }

    public void setIdade(int idade) {
        if (idade >= 0) {
            this.idade = idade;
        }
    }

    public Tutor getTutor() {
        return tutor;
    }

    public void setTutor(Tutor tutor) {
        this.tutor = tutor;
    }

    // Métodos abstratos: cada subclasse responde do seu jeito.

    /** Valor gravado na coluna animal.especie e enviado ao site: "cachorro" ou "gato". */
    public abstract String getEspecie();

    /** Nome da espécie para as pessoas: "Cachorro" ou "Gato". */
    public abstract String getTipo();

    /** "Porte: Grande" ou "Temperamento: Dócil". */
    public abstract String getCaracteristica();

    public abstract String emitirSom();
}
