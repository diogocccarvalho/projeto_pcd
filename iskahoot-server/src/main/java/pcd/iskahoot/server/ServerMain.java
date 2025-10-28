package pcd.iskahoot.server;

import java.util.ArrayList;
import java.util.List;

public class ServerMain {

    public static void main(String[] args) {
        TUI tui = new TUI();
        
        // Criar uma lista 'fake' de salas só para a TUI poder testar a opção "verSalas"
        // (O teu QuizLoader ainda não está a ser chamado, por isso passamos uma lista vazia de perguntas)
        ArrayList<GameState> salasAtivas = new ArrayList<>();

        System.out.println("Servidor IsKahoot (Modo Teste TUI) a arrancar...");
        System.out.println("CTRL+C para parar.");

        // Loop infinito para manter o menu a correr
        while (true) {
            String escolha = tui.mainMenu();

            switch (escolha) {
                case "criarSala":
                    tui.showMessage("Opção 'criarSala' selecionada.");
                    int numEquipas = tui.obterNumeroDeEquipas();
                    
                    if (numEquipas > 0) {
                        String idSala = "SALA-" + (salasAtivas.size() + 1); // Gerador de ID muito básico
                        tui.showMessage("A criar sala '" + idSala + "' com " + numEquipas + " equipas...");
                        
                        // Aqui, em vez de new ArrayList<>() , usarias o QuizLoader
                        GameState novaSala = new GameState(idSala, new ArrayList<>()); 
                        
                        salasAtivas.add(novaSala);
                        tui.showMessage("Sala criada. Código: " + idSala);
                    }
                    break;

                case "verSalas":
                    tui.showMessage("A mostrar salas ativas...");
                    tui.mostrarSalas(salasAtivas);
                    break;

                case "opçãoInválida":
                    // A TUI já trata da mensagem de erro
                    break;
            }
        }
    }
}