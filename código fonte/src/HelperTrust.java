import java.util.HashMap;
import java.util.Random;

public class HelperTrust {
	//to the Helper, the Double means the knowledge (between 0 and 1)
	private HashMap<String, Double> trustByCategory;
	
	public HelperTrust(){
		trustByCategory = new HashMap<String, Double>();
	}
	
	public Double getTrustByCategory(String s){
		if(trustByCategory.get(s) == null)
			return new Double(0.0);
		
		return trustByCategory.get(s);
	}
	
	public void addCategory(String category, double knowledge){
		trustByCategory.put(category, new Double(knowledge));
	}
	
	public boolean getAnswer(String category){
		Double knowledge;
		knowledge = trustByCategory.get(category);
		
		if(knowledge == null)
			return false;
		
		Random rand = new Random();
		int randomInt = rand.nextInt(100 + 1);
		
	    double randomDouble = randomInt/100.0;
	    
	    if(randomDouble <= knowledge.doubleValue())
	    	return true;
	    else
	    	return false;
	}
}
