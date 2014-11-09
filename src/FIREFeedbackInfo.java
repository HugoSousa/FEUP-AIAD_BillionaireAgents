
public class FIREFeedbackInfo {
	private int totalAnswers;
	private int positiveAnswers;
	
	FIREFeedbackInfo(int totalAnswers, int positiveAnswers){
		this.setTotalAnswers(totalAnswers);
		this.setPositiveAnswers(positiveAnswers);
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
}
