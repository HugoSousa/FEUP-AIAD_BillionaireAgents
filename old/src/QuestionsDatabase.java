import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;


public class QuestionsDatabase {
	
	private String FILENAME = "questions.txt";
	private ArrayList<Question> questions = new ArrayList<Question>();
	private String[] categories = new String[] {"desporto", "cultura", "arte", "historia", "cinema"};
	
	public QuestionsDatabase(){
		
		try {
			readFile(FILENAME);
		} catch (IOException e) {
			System.out.println("Error reading questions from " + FILENAME);
		}
		/*
		Question a = new Question("teste", new ArrayList<String>(Arrays.asList("a", "b", "c", "d")), 2, "desporto", 3);
		Question b = new Question("teste2", new ArrayList<String>(Arrays.asList("a1", "b1", "c1", "d1")), 1, "desporto", 3);
		Question c = new Question("teste3", new ArrayList<String>(Arrays.asList("a2", "b2", "c2", "d2")), 3, "desporto", 3);
		Question d = new Question("teste4", new ArrayList<String>(Arrays.asList("a3", "b3", "c3", "d3")), 0, "desporto", 3);
		Question e = new Question("teste5", new ArrayList<String>(Arrays.asList("a4", "b4", "c4", "d4")), 0, "desporto", 3);

		questions.add(a);
		questions.add(b);
		questions.add(c);
		questions.add(d);
		questions.add(e);
		 */
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

	public void readFile(String filename) throws IOException{
		BufferedReader br = new BufferedReader(new FileReader(filename));
		String line;
		while ((line = br.readLine()) != null) {
			// process the line.
			String[] questionElements = line.split(";");
			
			int difficulty = Integer.parseInt(questionElements[2]);
			String questionText = questionElements[3];
			ArrayList<String> questionOptions= new ArrayList<String>(Arrays.asList(questionElements[4], questionElements[5], questionElements[6], questionElements[7]));
			int correctAnswer = Integer.parseInt(questionElements[8]) - 1;
			
			//choose random category
			Random rand = new Random();
			int randomNum = rand.nextInt(categories.length);
			String category = categories[randomNum];
			
			Question q = new Question(questionText, questionOptions, correctAnswer, category, difficulty);
			questions.add(q);
		}
		br.close();


	}
}
