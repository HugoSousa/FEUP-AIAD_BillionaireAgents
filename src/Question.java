import java.util.ArrayList;


public class Question {
	private String text;
	private ArrayList<String> answers;
	private int correctAnswer;
	private String category;
	private int difficulty;
	
	Question(String text, ArrayList<String> answers, int correctAnswer, String category, int difficulty){
		this.text = text;
		this.answers= answers;
		this.correctAnswer = correctAnswer;
		this.category = category;
		this.difficulty = difficulty;
	}

	public String getText() {
		return text;
	}
	
	public String getCorrectAnswer(){
		return answers.get(correctAnswer);
	}
	
	public String getCategory(){
		return category;
	}
	
	public int getDifficulty(){
		return difficulty;
	}
	
	public ArrayList<String> getAnswerOptions(){
		return answers;
	}
}
