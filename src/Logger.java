import java.io.FileNotFoundException;
import java.io.PrintWriter;


public class Logger {
	private String fileName;
	private String log = "";
	
	public Logger(String filename){
		fileName = filename;
	}
	
	public void addToLog(String text){
		log += text;
		log += '\n';
	}
	
	public void addToLine(String text){
		log += text;
	}
	
	public void writeToFile(){
		PrintWriter out = null;
		try {
			out = new PrintWriter(fileName+".txt");
			out.println(log);
			out.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
	}
}
