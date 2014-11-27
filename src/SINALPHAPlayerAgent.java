import jade.core.AID;
import jade.lang.acl.ACLMessage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;


public class SINALPHAPlayerAgent extends GenericPlayerAgent {

	private double ALPHA_START = 2 * Math.PI;
	private double MIN_ALPHA = 3 * Math.PI / 2;
	private double MAX_ALPHA = 5 * Math.PI / 2;
	private double POSITIVE_LAMBDA = 1.0;
	private double NEGATIVE_LAMBDA = -1.5;
	private double OMEGA = Math.PI / 20; 
	
	private HashMap<HelperCategoryKey, Double> interactionTrust = new HashMap<HelperCategoryKey, Double>();
	
	public SINALPHAPlayerAgent(){
		super();
	}
	
	@Override
	protected String processQuestion(String category, String question, ArrayList<String> answerOptions){
		
		lastCategory = category;
		HashMap<AID,  ArrayList<Double>> witnessReputationByHelperCategory = super.askOtherPlayers(category);

		HashMap<AID, Double> interactionTrustByHelperCategory = processInteractionTrust(category);


		lastHelper = getBestHelper(interactionTrustByHelperCategory, null/*witnessReputationByHelperCategory*/);
		String answer =  super.askHelper(lastHelper, question, answerOptions);

		return answer;
	}

	@Override
	protected void lastAnswerIs(boolean result){
		
		double lambda;
		
		if(result)
			lambda = POSITIVE_LAMBDA;
		else 
			lambda = NEGATIVE_LAMBDA;
		
		HelperCategoryKey key = new HelperCategoryKey(lastHelper,lastCategory);
		Double alpha = interactionTrust.get(key);
		
		
		if (alpha == null) {
			alpha = new Double(ALPHA_START);
			alpha = alpha + lambda * OMEGA;
			if(alpha < MIN_ALPHA)
				alpha = MIN_ALPHA;
			else if(alpha > MAX_ALPHA)
				alpha = MAX_ALPHA;
			
			interactionTrust.put(key, alpha);
		}else{
			alpha = alpha + lambda * OMEGA;
			if(alpha < MIN_ALPHA)
				alpha = MIN_ALPHA;
			else if(alpha > MAX_ALPHA)
				alpha = MAX_ALPHA;
			
			interactionTrust.put(key, alpha);
		}
		
		System.out.println("UPDATE ALFA: " + interactionTrust.get(key));
		System.out.println("UPDATE ALFA: " + Math.sin(alpha));
		
	}
	
	private HashMap<AID, Double> processInteractionTrust(String category) {
		
		HashMap<AID, Double> interactionTrustByHelperCategory = new HashMap<AID, Double>();
		
		for(int i = 0; i < helpers.size(); i++){
			AID helper = helpers.get(i);
			
			Double helperRating = interactionTrust.get(new HelperCategoryKey(helper, category));
			
			if (helperRating != null) {
				interactionTrustByHelperCategory.put(helper, new Double(helperRating.doubleValue()));

			}
		}
		return interactionTrustByHelperCategory;
	}
	
	private AID getBestHelper(HashMap<AID, Double> interactionTrustByHelperCategory, HashMap<AID, ArrayList<Double>> witnessReputationByHelperCategory) {

		HashMap<AID,Double> finalTrust = new HashMap<AID, Double>();
		
		for(AID helper: helpers){
			
			Double it = interactionTrustByHelperCategory.get(helper);
			
			if(it == null){
				it = new Double(0.0);
			}
			
			finalTrust.put(helper, it);			
		}
		
		ArrayList<AID> best = new ArrayList<AID>();
		
		double max = -1;
		for (HashMap.Entry<AID, Double> entry : finalTrust.entrySet()) {
		    AID helper = entry.getKey();
		    Double trust = entry.getValue();
		    
		    if (trust.doubleValue() > max) {
		    	best.clear();
		    	best.add(helper);
		    	max = trust.doubleValue();
		    }else if (trust.doubleValue() == max) {
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

	@Override
	protected void answerFeedback(ACLMessage msg) {
		String category = msg.getContent();
		HashMap<String, Object> sendInfo = new HashMap<String, Object>();

		for(int i = 0; i < helpers.size(); i++){

			AID helper = helpers.get(i);
			Double helperRating = interactionTrust.get(new HelperCategoryKey(helper, category));
			
			
			
			//se nao existe interação direta com o helper para aquela categoria
			if(helperRating == null)
			{
				System.out.println(getLocalName() + ": NAO TENHO INFORMACAO DO HELPER " + helper.getLocalName());
			}
			else{
				System.out.println(getLocalName() + ": TENHO INFORMACAO DO HELPER " + helper.getLocalName() + ": " + Math.sin(helperRating));			
				sendInfo.put(helper.getLocalName(), Math.sin(helperRating));
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
