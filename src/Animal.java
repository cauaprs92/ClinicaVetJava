// Abstracao: Animal define o que todo paciente da clinica tem em comum,
// mas nao pode ser instanciada diretamente.
public abstract class Animal {

    // Encapsulamento: os atributos sao private e so podem ser acessados
    // pelos metodos get e set.
    private String nome;
    private int idade;
    private String nomeDono;
    private String telefoneDono;

    public Animal(String nome, int idade, String nomeDono, String telefoneDono) {
        this.nome = nome;
        this.idade = idade;
        this.nomeDono = nomeDono;
        this.telefoneDono = telefoneDono;
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

    public String getNomeDono() {
        return nomeDono;
    }

    public void setNomeDono(String nomeDono) {
        this.nomeDono = nomeDono;
    }

    public String getTelefoneDono() {
        return telefoneDono;
    }

    public void setTelefoneDono(String telefoneDono) {
        this.telefoneDono = telefoneDono;
    }

    // Metodo abstrato: cada subclasse obrigatoriamente emite o seu proprio som.
    public abstract String emitirSom();

    // Cada subclasse informa o seu tipo e a sua caracteristica especifica.
    public abstract String getTipo();

    public abstract String getCaracteristica();
}
