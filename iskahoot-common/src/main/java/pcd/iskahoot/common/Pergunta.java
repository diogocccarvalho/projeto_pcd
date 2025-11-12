package pcd.iskahoot.common;

import java.util.List;

public class Pergunta {

    private String question;
    private int points;
    private int correct;
    private List<String> options;
    private TipoPergunta tipo = TipoPergunta.INDIVIDUAL;

    public String getQuestion() {
        return question;
    }

    public int getPoints() {
        return points;
    }

    public int getCorrect() {
        return correct;
    }

    public List<String> getOptions() {
        return options;
    }

    public TipoPergunta getTipo() {
        return tipo;
    }
}