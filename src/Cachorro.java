// Heranca: Cachorro herda os atributos e metodos da classe Animal.
public class Cachorro extends Animal {

    private String porte;

    public Cachorro(String nome, int idade, String nomeDono, String telefoneDono, String porte) {
        // super chama o construtor da classe pai (Animal)
        super(nome, idade, nomeDono, telefoneDono);
        this.porte = porte;
    }

    public String getPorte() {
        return porte;
    }

    public void setPorte(String porte) {
        this.porte = porte;
    }

    @Override
    public String emitirSom() {
        return "Au Au!";
    }

    @Override
    public String getTipo() {
        return "Cachorro";
    }

    @Override
    public String getCaracteristica() {
        return "Porte: " + porte;
    }
}
