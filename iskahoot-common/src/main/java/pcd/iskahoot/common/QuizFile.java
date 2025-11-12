package pcd.iskahoot.common;

import java.util.List;

public class QuizFile implements java.io.Serializable{

    private static final long serialVersionUID = 1L;
    private List<Quiz> quizzes;

    public List<Quiz> getQuizzes() {
        return quizzes;
    }
}