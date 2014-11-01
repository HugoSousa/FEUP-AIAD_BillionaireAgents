import java.util.ArrayList;
import java.util.Arrays;


public class QuestionsDatabase {
	ArrayList<Question> questions = new ArrayList<Question>();
	
	public QuestionsDatabase(){
		Question a = new Question("teste", new ArrayList<String>(Arrays.asList("a", "b", "c", "d")), 2, "desporto", 3);
		Question b = new Question("teste2", new ArrayList<String>(Arrays.asList("a1", "b1", "c1", "d1")), 1, "cultura geral", 3);
		Question c = new Question("teste3", new ArrayList<String>(Arrays.asList("a2", "b2", "c2", "d2")), 3, "literatura", 3);
		Question d = new Question("teste4", new ArrayList<String>(Arrays.asList("a3", "b3", "c3", "d3")), 0, "ciência", 3);
		Question e = new Question("teste5", new ArrayList<String>(Arrays.asList("a4", "b4", "c4", "d4")), 0, "desporto", 3);
	
		questions.add(a);
		questions.add(b);
		questions.add(c);
		questions.add(d);
		questions.add(e);
	}
	
	public Question get(int index){
		return questions.get(index);
	}
	
	public void remove(Question q){
		questions.remove(q);
	}
	
	public int size(){
		return questions.size();
	}
	
	public String getCategory(String question){
		for(int i=0; i < questions.size(); i++){
			if(questions.get(i).getText().equals(question))
				return questions.get(i).getCategory();
		}
		
		return null;
	}
	
	public String getCorrectAnswer(String question){
		for(int i=0; i < questions.size(); i++){
			if(questions.get(i).getText().equals(question))
				return questions.get(i).getCorrectAnswer();
		}
		
		return null;
	}
}
