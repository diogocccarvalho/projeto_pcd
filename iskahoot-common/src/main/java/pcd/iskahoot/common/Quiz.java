package pcd.iskahoot.common;

import java.util.List;

public class Quiz implements java.io.Serializable{

    private static final long serialVersionUID = 1L;
    private String name;
    private List<Pergunta> questions;

    public String getName() {
        return name;
    }

    public List<Pergunta> getQuestions() {
        return questions;
    }
}