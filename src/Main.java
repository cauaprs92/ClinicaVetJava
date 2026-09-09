import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        Scanner entrada = new Scanner(System.in);
        int opcao = 0;

        while (opcao != 5) {
            System.out.println();
            System.out.println("====================================");
            System.out.println("        CLINICA VETERINARIA");
            System.out.println("====================================");
            System.out.println();
            System.out.println("1 - Agendar consulta");
            System.out.println("2 - Consultar agenda");
            System.out.println("3 - Editar consulta");
            System.out.println("4 - Cancelar consulta");
            System.out.println("5 - Sair");
            System.out.println();
            System.out.print("Escolha uma opcao: ");

            opcao = AgendaManager.lerInteiro(entrada);

            switch (opcao) {
                case 1:
                    AgendaManager.agendar(entrada);
                    break;
                case 2:
                    AgendaManager.listar();
                    break;
                case 3:
                    AgendaManager.editar(entrada);
                    break;
                case 4:
                    AgendaManager.excluir(entrada);
                    break;
                case 5:
                    System.out.println("Sistema encerrado. Ate logo!");
                    break;
                default:
                    System.out.println("Opcao invalida! Escolha um numero de 1 a 5.");
            }
        }

        entrada.close();
    }
}
