import java.util.HashMap;


public class FIREPlayerTrust {
	private HashMap<String, FIREFeedbackInfo> trustByCategory;

	public FIREPlayerTrust(){
		trustByCategory = new HashMap<String, FIREFeedbackInfo>();
	}

	public FIREFeedbackInfo getInfoByCategory(String s){
		
		if(trustByCategory.get(s) == null){
			trustByCategory.put(s, new FIREFeedbackInfo(0, 0));
		}
		
		return trustByCategory.get(s);

	}
	

	public void addNewAnswer(String category, boolean positiveAnswer){
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
