import jade.core.AID;
import jade.lang.acl.ACLMessage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;


public class BETAPlayerAgent extends GenericPlayerAgent{

	//private int correctAnswers = 0;
	//private int wrongAnswers = 0;
	private HashMap<HelperCategoryKey, FeedbackInfo> interactionTrust = new HashMap<HelperCategoryKey, FeedbackInfo>();

	public BETAPlayerAgent(){
		super();
	}

	@Override
	protected String processQuestion(String category, String question, ArrayList<String> answerOptions) {

		lastCategory = category;

		HashMap<AID,  ArrayList<Double>> witnessReputationByHelperCategory = super.askOtherPlayers(category);

		HashMap<AID, FeedbackInfo> interactionTrustByHelperCategory = processInteractionTrust(category);

		lastHelper = getBestHelper(interactionTrustByHelperCategory, null/*witnessReputationByHelperCategory*/);
		String answer =  super.askHelper(lastHelper, question, answerOptions);

		return answer;
	}

	private AID getBestHelper(HashMap<AID, FeedbackInfo> interactionTrustByHelperCategory, Object object) {

		HashMap<AID, Double> finalTrust = new HashMap<AID, Double>();
		
		for(AID helper: helpers){ 
			FeedbackInfo it = interactionTrustByHelperCategory.get(helper);
			
			if(it == null)
				finalTrust.put(helper, 0.0);
			else{
				double r = it.getRating();
				double s = it.getTotalRatings() - r;
				
				double rep = (r - s)/(r + s + 2);
				
				System.out.println("REPUTATION: " + rep);
				
				finalTrust.put(helper, rep);
			}
				
		}

		ArrayList<AID> best = new ArrayList<AID>();

		double max = -1;
		for (HashMap.Entry<AID, Double> entry : finalTrust.entrySet()) {
			AID helper = entry.getKey();
			Double trustValue = entry.getValue();

			if (trustValue.doubleValue() > max) {
				best.clear();
				best.add(helper);
				max = trustValue.doubleValue();
			}else if (trustValue.doubleValue() == max) {
				best.add(helper);
			}  		
		}

		AID selectedHelper = null;
		if(best.size() > 1){
			Random rand = new Random();
			int randomHelper = rand.nextInt(best.size());
			selectedHelper = (AID) best.toArray()[randomHelper];
			best.clear();
		}else
			selectedHelper = (AID) best.toArray()[0];


		System.out.println("Escolhi o helper " + selectedHelper.getLocalName());
		return selectedHelper;
		
	}

	private HashMap<AID, FeedbackInfo> processInteractionTrust(String category){

		HashMap<AID, FeedbackInfo> interactionTrustByHelperCategory = new HashMap<AID, FeedbackInfo>();

		for(int i = 0; i < helpers.size(); i++){
			AID helper = helpers.get(i);

			FeedbackInfo helperRating = interactionTrust.get(new HelperCategoryKey(helper, category));

			if(helperRating != null){
				interactionTrustByHelperCategory.put(helper, helperRating);
			}
		}
		return interactionTrustByHelperCategory;

	};

	@Override
	protected void lastAnswerIs(boolean result) {
		
		HelperCategoryKey key = new HelperCategoryKey(lastHelper, lastCategory);
		FeedbackInfo lastRating = interactionTrust.get(key);
		
		
		if(lastRating == null){
			if(result)
				interactionTrust.put(key, new FeedbackInfo(1, 1));
			else
				interactionTrust.put(key, new FeedbackInfo(0, 1));
		}
		else{
			double correctRating = lastRating.getRating(); 
			int totalRatings = lastRating.getTotalRatings();
			
			if(result)
				interactionTrust.put(key, new FeedbackInfo(correctRating + 1, totalRatings + 1));
			else
				interactionTrust.put(key, new FeedbackInfo(correctRating, totalRatings + 1));
		}
			
		System.out.println("INTERACTION TRUST: " + interactionTrust.get(new HelperCategoryKey(lastHelper, lastCategory)));

	}

	@Override
	protected void answerFeedback(ACLMessage msg) {
		
		String category = msg.getContent();
		HashMap<String, Object> sendInfo = new HashMap<String, Object>();

		for(int i = 0; i < helpers.size(); i++){

			AID helper = helpers.get(i);
			FeedbackInfo helperRatings = interactionTrust.get(new HelperCategoryKey(helper, category));

			//se nao existe interação direta com o helper para aquela categoria
			if(helperRatings == null)
			{
				System.out.println(getLocalName() + ": NAO TENHO INFORMACAO DO HELPER " + helper.getLocalName());
			}
			else{
				System.out.println(getLocalName() + ": TENHO INFORMACAO DO HELPER " + helper.getLocalName() + ": " + helperRatings.getTotalRatings() + " RATINGS");			
				
				double r = helperRatings.getRating();
				double s = helperRatings.getTotalRatings() - r;
				
				double rep = (r - s)/(r + s + 2);
				
				sendInfo.put(helper.getLocalName(), new Double(rep));
			}
		}

		String answer = null;
		//nao tem informacao de nenhum helper
		if(sendInfo.isEmpty()){
			answer = "none";
		}
		else{
			answer = Utils.JSONEncode(sendInfo);
		}

		ACLMessage feedbackMsg = msg.createReply();
		feedbackMsg.setPerformative(ACLMessage.INFORM);
		feedbackMsg.setContent(answer);

		send(feedbackMsg);

	}

}
