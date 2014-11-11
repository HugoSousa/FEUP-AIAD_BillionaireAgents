
public class FIREFeedbackInfo {
	private int totalAnswers;
	private int positiveAnswers;
	private int feedbacks;
	
	FIREFeedbackInfo(int totalAnswers, int positiveAnswers){
		this.setTotalAnswers(totalAnswers);
		this.setPositiveAnswers(positiveAnswers);
		feedbacks = 0;
	}

	public int getTotalAnswers() {
		return totalAnswers;
	}

	public void setTotalAnswers(int totalAnswers) {
		this.totalAnswers = totalAnswers;
	}

	public int getPositiveAnswers() {
		return positiveAnswers;
	}

	public void setPositiveAnswers(int positiveAnswers) {
		this.positiveAnswers = positiveAnswers;
	}
	
	public String toString(){
		return positiveAnswers + "/" + totalAnswers;
	}

	public void addPositiveAnswers(int pa) {
		positiveAnswers+= pa;
		
	}
	
	public void addTotalAnswers(int ta) {
		totalAnswers+= ta;
		
	}
	
	public void addFeedback(){
		feedbacks++;
	}
	
	public int getFeedbacks(){
		return feedbacks;
	}
}
