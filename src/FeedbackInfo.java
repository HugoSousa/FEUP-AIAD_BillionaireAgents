public class FeedbackInfo{

	private double rating;
	private int totalRatings;
	
	public FeedbackInfo(double rating, int totalRatings) {
		super();
		this.setRating(rating);
		this.setTotalRatings(totalRatings);
	}

	public double getRating() {
		return rating;
	}

	public void setRating(double rating) {
		this.rating = rating;
	}

	public int getTotalRatings() {
		return totalRatings;
	}

	public void setTotalRatings(int totalRatings) {
		this.totalRatings = totalRatings;
	}
	
	public String toString(){
		return rating + "/" + totalRatings;
	}
	
}
