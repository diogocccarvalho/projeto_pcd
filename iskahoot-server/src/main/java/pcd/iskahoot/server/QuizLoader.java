package pcd.iskahoot.server;

import com.google.gson.Gson;
import pcd.iskahoot.common.Pergunta;
import pcd.iskahoot.common.Quiz;
import pcd.iskahoot.common.QuizFile;

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
        QuizFile quizFile = gson.fromJson(reader, QuizFile.class);

        if (quizFile == null || quizFile.getQuizzes() == null) {
            throw new RuntimeException("Formato de JSON inválido.");
        }

        return quizFile.getQuizzes().stream()
                .filter(quiz -> Objects.equals(quiz.getName(), nomeDoQuiz))
                .findFirst()
                .map(Quiz::getQuestions)
                .orElseThrow(() -> new RuntimeException("Quiz com o nome '" + nomeDoQuiz + "' não encontrado no JSON."));
    }
}