import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;


public class FeedbackInfo implements java.io.Serializable{

	private static final long serialVersionUID = -2148458889630846090L;
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

	public String serialize() {
		 String serializedObject = "";

		 // serialize the object
		 try {
		     ByteArrayOutputStream bo = new ByteArrayOutputStream();
		     ObjectOutputStream so = new ObjectOutputStream(bo);
		     so.writeObject(this);
		     so.flush();
		     serializedObject = bo.toString();
		 } catch (Exception e) {
		     System.out.println(e);
		 }
		 return serializedObject;
	}
	
	public static FeedbackInfo deserialize(String serializedObject) {
		 FeedbackInfo obj = null;
		try {
		     byte b[] = serializedObject.getBytes(); 
		     ByteArrayInputStream bi = new ByteArrayInputStream(b);
		     ObjectInputStream si = new ObjectInputStream(bi);
		      obj = (FeedbackInfo) si.readObject();
		 } catch (Exception e) {
		     System.out.println(e);
		 }
		return obj;
	}
	
}
