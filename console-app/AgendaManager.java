import java.util.Scanner;

public class AgendaManager {

    // Vetor de controle: cada posicao representa um dos 10 horarios do dia
    private static Consulta[] agenda = new Consulta[10];

    private static String[] horarios = {"08:00", "09:00", "10:00", "11:00", "12:00",
                                        "14:00", "15:00", "16:00", "17:00", "18:00"};

    public static void agendar(Scanner entrada) {
        if (agendaCheia()) {
            System.out.println("Erro: a agenda esta cheia, nao existem horarios disponiveis.");
            return;
        }

        mostrarHorarios();

        System.out.print("Escolha o horario (1 a 10): ");
        int indice = lerInteiro(entrada) - 1;

        if (indice < 0 || indice > 9) {
            System.out.println("Erro: horario invalido. Escolha um numero de 1 a 10.");
            return;
        }

        if (agenda[indice] != null) {
            System.out.println("Erro: este horario ja esta ocupado.");
            return;
        }

        Animal paciente = lerAnimal(entrada);
        if (paciente == null) {
            return;
        }

        System.out.print("Sintomas / queixa principal: ");
        String sintomas = entrada.nextLine();

        agenda[indice] = new Consulta(paciente, horarios[indice], sintomas);
        System.out.println("Consulta agendada com sucesso no horario " + horarios[indice] + "!");
    }

    public static void listar() {
        System.out.println();
        System.out.println("========== AGENDA DO DIA ==========");
        System.out.println();

        if (agendaVazia()) {
            System.out.println("Nenhum atendimento foi agendado para hoje.");
            System.out.println();
        }

        for (int i = 0; i < agenda.length; i++) {
            if (agenda[i] == null) {
                System.out.println((i + 1) + " - " + horarios[i] + " - [Disponivel]");
            } else {
                Consulta consulta = agenda[i];
                Animal paciente = consulta.getPaciente();

                System.out.println((i + 1) + " - " + consulta.getHorario());
                System.out.println("   Animal: " + paciente.getNome());
                System.out.println("   Idade: " + paciente.getIdade() + " ano(s)");
                System.out.println("   Tipo: " + paciente.getTipo());
                System.out.println("   Dono: " + paciente.getNomeDono());
                System.out.println("   Telefone: " + paciente.getTelefoneDono());
                System.out.println("   Sintomas: " + consulta.getSintomas());
                System.out.println("   " + paciente.getCaracteristica());
                // Polimorfismo: o metodo emitirSom executado depende do objeto
                // guardado no atributo paciente (Cachorro ou Gato).
                System.out.println("   Som: " + paciente.emitirSom());
            }
            System.out.println();
        }
    }

    public static void editar(Scanner entrada) {
        System.out.print("Informe o horario que deseja editar (1 a 10): ");
        int indice = lerInteiro(entrada) - 1;

        if (indice < 0 || indice > 9) {
            System.out.println("Erro: horario invalido. Escolha um numero de 1 a 10.");
            return;
        }

        if (agenda[indice] == null) {
            System.out.println("Erro: nao existe consulta cadastrada neste horario.");
            return;
        }

        System.out.println("Consulta atual: " + agenda[indice].getPaciente().getNome()
                + " (dono: " + agenda[indice].getPaciente().getNomeDono() + ")");
        System.out.println("Informe os novos dados da consulta:");

        Animal paciente = lerAnimal(entrada);
        if (paciente == null) {
            return;
        }

        System.out.print("Sintomas / queixa principal: ");
        String sintomas = entrada.nextLine();

        agenda[indice].setPaciente(paciente);
        agenda[indice].setSintomas(sintomas);
        System.out.println("Consulta editada com sucesso!");
    }

    public static void excluir(Scanner entrada) {
        System.out.print("Informe o horario que deseja cancelar (1 a 10): ");
        int indice = lerInteiro(entrada) - 1;

        if (indice < 0 || indice > 9) {
            System.out.println("Erro: horario invalido. Escolha um numero de 1 a 10.");
            return;
        }

        if (agenda[indice] == null) {
            System.out.println("Erro: nao existe consulta cadastrada neste horario.");
            return;
        }

        agenda[indice] = null;
        System.out.println("Consulta cancelada com sucesso!");
    }

    // Le os dados do paciente e devolve um Cachorro ou um Gato.
    // O retorno e do tipo Animal para demonstrar o polimorfismo.
    private static Animal lerAnimal(Scanner entrada) {
        System.out.println("Qual o tipo de animal?");
        System.out.println("1 - Cachorro");
        System.out.println("2 - Gato");
        System.out.print("Opcao: ");
        int tipo = lerInteiro(entrada);

        if (tipo != 1 && tipo != 2) {
            System.out.println("Erro: tipo de animal invalido.");
            return null;
        }

        System.out.print("Nome do animal: ");
        String nome = entrada.nextLine();

        System.out.print("Idade do animal: ");
        int idade = lerInteiro(entrada);
        if (idade < 0) {
            System.out.println("Erro: idade invalida.");
            return null;
        }

        System.out.print("Nome do dono: ");
        String nomeDono = entrada.nextLine();

        System.out.print("Telefone do dono: ");
        String telefoneDono = entrada.nextLine();

        Animal paciente;

        if (tipo == 1) {
            System.out.print("Porte (Pequeno, Medio ou Grande): ");
            String porte = entrada.nextLine();
            paciente = new Cachorro(nome, idade, nomeDono, telefoneDono, porte);
        } else {
            System.out.print("Temperamento (Docil, Arredio ou Brincalhao): ");
            String temperamento = entrada.nextLine();
            paciente = new Gato(nome, idade, nomeDono, telefoneDono, temperamento);
        }

        return paciente;
    }

    private static void mostrarHorarios() {
        System.out.println("Horarios do dia:");
        for (int i = 0; i < agenda.length; i++) {
            if (agenda[i] == null) {
                System.out.println((i + 1) + " - " + horarios[i] + " - [Disponivel]");
            } else {
                System.out.println((i + 1) + " - " + horarios[i] + " - [Ocupado]");
            }
        }
    }

    private static boolean agendaCheia() {
        for (int i = 0; i < agenda.length; i++) {
            if (agenda[i] == null) {
                return false;
            }
        }
        return true;
    }

    private static boolean agendaVazia() {
        for (int i = 0; i < agenda.length; i++) {
            if (agenda[i] != null) {
                return false;
            }
        }
        return true;
    }

    // Le um numero digitado pelo usuario. Devolve -1 quando o texto
    // digitado nao for um numero, evitando que o programa pare.
    public static int lerInteiro(Scanner entrada) {
        String texto = entrada.nextLine();

        if (texto.length() == 0) {
            return -1;
        }

        for (int i = 0; i < texto.length(); i++) {
            if (texto.charAt(i) < '0' || texto.charAt(i) > '9') {
                return -1;
            }
        }

        return Integer.parseInt(texto);
    }
}
