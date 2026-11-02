// Heranca: Gato herda os atributos e metodos da classe Animal.
public class Gato extends Animal {

    private String temperamento;

    public Gato(String nome, int idade, String nomeDono, String telefoneDono, String temperamento) {
        // super chama o construtor da classe pai (Animal)
        super(nome, idade, nomeDono, telefoneDono);
        this.temperamento = temperamento;
    }

    public String getTemperamento() {
        return temperamento;
    }

    public void setTemperamento(String temperamento) {
        this.temperamento = temperamento;
    }

    @Override
    public String emitirSom() {
        return "Miau!";
    }

    @Override
    public String getTipo() {
        return "Gato";
    }

    @Override
    public String getCaracteristica() {
        return "Temperamento: " + temperamento;
    }
}
