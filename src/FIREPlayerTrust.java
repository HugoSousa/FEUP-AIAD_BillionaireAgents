import java.util.HashMap;
import java.util.Random;


public class FIREPlayerTrust {
	private HashMap<String, FIREFeedbackInfo> trustByCategory;

	public FIREPlayerTrust(){
		trustByCategory = new HashMap<String, FIREFeedbackInfo>();
	}

	public FIREFeedbackInfo getInfoByCategory(String s){
		return trustByCategory.get(s);
	}
	
	public double getTrustByCategory(String s){
		FIREFeedbackInfo ffi = trustByCategory.get(s);
		
		if(ffi == null)
			return 0.0;
		
		double trust = ffi.getPositiveAnswers()/ffi.getTotalAnswers();

		return trust;
	}

	public void addCategory(String category, boolean positiveAnswer){
		FIREFeedbackInfo ffi = trustByCategory.get(category);
		
		if(ffi == null){
			int positiveAnswers = 0;
			
			if(positiveAnswer)
				positiveAnswers = 1;
			
			trustByCategory.put(category, new FIREFeedbackInfo(positiveAnswers, 1));
		}else{
			int positiveAnswers = ffi.getPositiveAnswers();
			int totalAnswers = ffi.getTotalAnswers();
			
			
			if(positiveAnswer){
				positiveAnswers++;
				ffi.setPositiveAnswers(positiveAnswers);
			}
			
			totalAnswers++;
			ffi.setTotalAnswers(totalAnswers);
		}	
	}
	
	public void addCategory(String category, FIREFeedbackInfo info){
		FIREFeedbackInfo ffi = trustByCategory.get(category);
		
		if(ffi == null){
			trustByCategory.put(category, info);
		}else{
			ffi = info;
		}	
	}

}
