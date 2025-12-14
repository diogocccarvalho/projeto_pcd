package pcd.iskahoot.server;

import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;

class TUI {

  private final Scanner userInput = new Scanner(System.in);

  public String mainMenu() {
    System.out.println("\n--- MENU DO SERVIDOR IsKahoot ---");
    System.out.println("1. Criar sala de jogo");
    System.out.println("2. Visualizar salas criadas");
    System.out.print("Escolher opção (1 - 2): ");

    String choice = userInput.nextLine();

    switch (choice) {
      case "1":
        return "criarSala";
      case "2":
        return "verSalas";
      default:
        System.out.println("'" + choice + "' não é uma opção válida!");
        return "opçãoInválida";
    }
  }

  public GameConfig obterConfiguracaoJogo() {
    System.out.println("\n--- CONFIGURAR NOVA SALA ---");
    int numEquipas = obterNumero("Número de equipas: ");
    if (numEquipas == -1) return null;

    int jogadoresPorEquipa = obterNumero("Jogadores por equipa: ");
    if (jogadoresPorEquipa == -1) return null;
    
    int numPerguntas = obterNumero("Número de perguntas: ");
    if (numPerguntas == -1) return null;

    int tempo = obterNumero("Tempo por pergunta (segundos): ");
    if (tempo == -1) return null;

    return new GameConfig(numEquipas, jogadoresPorEquipa, numPerguntas, tempo);
  }

  private int obterNumero(String prompt) {
    System.out.print(prompt);
    try {
      int numero = userInput.nextInt();
      userInput.nextLine(); 

      if (numero > 0) {
        return numero;
      } else {
        System.out.println("Erro: O número deve ser um inteiro positivo.");
        return -1;
      }
    } catch (InputMismatchException e) {
      System.out.println("Erro: Por favor, introduza um número inteiro válido.");
      userInput.nextLine(); 
      return -1;
    }
  }

  public void mostrarSalas(List<GameState> salas) {
    System.out.println("\n--- SALAS DE JOGO ATIVAS ---");
    if (salas == null || salas.isEmpty()) {
      System.out.println("Não existem salas ativas no momento.");
    } else {
      for (GameState sala : salas) {
        // O método toString() de GameState foi melhorado para mostrar mais detalhes
        System.out.println(sala.toString()); 
      }
    }
  }

  public void showMessage(String message) {
    System.out.println(message);
  }
}
