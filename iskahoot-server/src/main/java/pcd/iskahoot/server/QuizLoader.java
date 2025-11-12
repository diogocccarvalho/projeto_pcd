package pcd.iskahoot.server;

import com.google.gson.Gson;
import pcd.iskahoot.common.Pergunta;
import pcd.iskahoot.common.Quiz;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;
import java.util.Objects;

public class QuizLoader {

    public static List<Pergunta> carregarPerguntasDoQuiz(String nomeFicheiroJson, String nomeDoQuiz) {
        
        Gson gson = new Gson();
        
        InputStream is = QuizLoader.class.getClassLoader().getResourceAsStream(nomeFicheiroJson);
        if (is == null) {
            throw new RuntimeException("Ficheiro JSON não encontrado na pasta resources: " + nomeFicheiroJson);
        }

        Reader reader = new InputStreamReader(is);

        Quiz quiz = gson.fromJson(reader, Quiz.class);

        if (quiz == null || quiz.getQuestions() == null) {
            throw new RuntimeException("Formato de JSON inválido.");
        }
        if (!Objects.equals(quiz.getName(), nomeDoQuiz)) {
            throw new RuntimeException("O quiz no ficheiro (" + quiz.getName() + ") não é o quiz pedido (" + nomeDoQuiz + ").");
        }

        return quiz.getQuestions();
    }
}