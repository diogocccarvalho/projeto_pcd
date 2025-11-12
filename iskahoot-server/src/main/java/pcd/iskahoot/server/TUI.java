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

  public int obterNumeroDeEquipas() {
    System.out.print("Introduza o número de equipas para a nova sala: ");
    try {
      int numEquipas = userInput.nextInt();
      userInput.nextLine();

      if (numEquipas > 0) {
        return numEquipas;
      } else {
        System.out.println("O número de equipas deve ser positivo.");
        return -1; // Sinaliza um erro para a main
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
        System.out.println(sala);
      }
    }
  }

  public void showMessage(String message) {
    System.out.println(message);
  }
}
