public class Consulta {

    // Polimorfismo: paciente pode receber um Cachorro ou um Gato
    private Animal paciente;
    private String horario;
    private String sintomas;

    public Consulta(Animal paciente, String horario, String sintomas) {
        this.paciente = paciente;
        this.horario = horario;
        this.sintomas = sintomas;
    }

    public Animal getPaciente() {
        return paciente;
    }

    public void setPaciente(Animal paciente) {
        this.paciente = paciente;
    }

    public String getHorario() {
        return horario;
    }

    public void setHorario(String horario) {
        this.horario = horario;
    }

    public String getSintomas() {
        return sintomas;
    }

    public void setSintomas(String sintomas) {
        this.sintomas = sintomas;
    }
}
