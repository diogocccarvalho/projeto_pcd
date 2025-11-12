package pcd.iskahoot.server;

import java.util.ArrayList;
import java.util.List;
import pcd.iskahoot.common.Pergunta;

public class ServerMain {

    public static void main(String[] args) {
        TUI tui = new TUI();
        ArrayList<GameState> salasAtivas = new ArrayList<>();

        System.out.println("Servidor IsKahoot (Modo Teste TUI) a arrancar...");
        System.out.println("CTRL+C para parar.");

        while (true) {
            String escolha = tui.mainMenu();

            switch (escolha) {
                case "criarSala":
                    tui.showMessage("Opção 'criarSala' selecionada.");
                    int numEquipas = tui.obterNumeroDeEquipas();
                    
                    if (numEquipas > 0) {
                        String idSala = "SALA-" + (salasAtivas.size() + 1);
                        
                        try {
                            List<Pergunta> perguntas = QuizLoader.carregarPerguntasDoQuiz("quizzes.json", "PCD-1");
                            GameState novaSala = new GameState(idSala, perguntas);
                            salasAtivas.add(novaSala);
                            tui.showMessage("Sala criada com " + perguntas.size() + " perguntas. Código: " + idSala);
                        
                        } catch (Exception e) {
                            tui.showMessage("!!! ERRO ao carregar quiz: " + e.getMessage());
                        }
                    }
                    break;

                case "verSalas":
                    tui.showMessage("A mostrar salas ativas...");
                    tui.mostrarSalas(salasAtivas);
                    break;

                case "opçãoInválida":
                    break;
            }
        }
    }
}