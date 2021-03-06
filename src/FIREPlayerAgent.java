import jade.core.AID;
import jade.lang.acl.ACLMessage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;


@SuppressWarnings("serial")
public class FIREPlayerAgent extends GenericPlayerAgent{

	private HashMap<HelperCategoryKey, ArrayList<Double>> interactionTrust = new HashMap<HelperCategoryKey, ArrayList<Double>>();
	private int THRESHOLD_IT = 5;
	private int THRESHOLD_W = 2;
	private int NEWCOMER_THRESHOLD = 5;

	
	public FIREPlayerAgent(){
		super();

		//System.out.println("FIRE PLAYER AGENT CONSTRUCTOR");
	}

	@Override
	//processa o conte�do da quest�o, de acordo com o modelo
	public String processQuestion(String category, String question, ArrayList<String> answerOptions) {
		
		//System.out.println(interactionTrust);
		
		lastCategory = category;
		HashMap<AID,  ArrayList<Double>> witnessReputationByHelperCategory = super.askOtherPlayers(category);

		HashMap<AID, FeedbackInfo> interactionTrustByHelperCategory = processInteractionTrust(category);


		lastHelper = getBestHelper(interactionTrustByHelperCategory, witnessReputationByHelperCategory);
		String answer =  super.askHelper(lastHelper, question, answerOptions);

		return answer;
	}


	private HashMap<AID, FeedbackInfo> processInteractionTrust(String category) {

		HashMap<AID, FeedbackInfo> interactionTrustByHelperCategory = new HashMap<AID, FeedbackInfo>();

		for(int i = 0; i < helpers.size(); i++){
			double sumRatings = 0.0;
			AID helper = helpers.get(i);

			ArrayList<Double> helperRatings = interactionTrust.get(new HelperCategoryKey(helper, category));

			double trust_IT;
			int size;
			if (helperRatings != null) {

				for(int j = 0; j < helperRatings.size(); j++)
					sumRatings += helperRatings.get(j).doubleValue();

				trust_IT = sumRatings / (double) helperRatings.size();
				size = helperRatings.size();
				interactionTrustByHelperCategory.put(helper, new FeedbackInfo(trust_IT, size));

			}
		}
		return interactionTrustByHelperCategory;
	}



	private AID getBestHelper(HashMap<AID, FeedbackInfo> interactionTrustByHelperCategory, HashMap<AID, ArrayList<Double>> witnessReputationByHelperCategory) {

		HashMap<AID,FeedbackInfo> finalTrust = new HashMap<AID, FeedbackInfo>(); // usando o totalRatings para detetar newcomers


		for(AID helper: helpers){
			double ro_IT, trust_IT, ro_WR, trust_WR, trust;
			int newcomerCount;
			FeedbackInfo it = interactionTrustByHelperCategory.get(helper);
			ArrayList<Double> wr = witnessReputationByHelperCategory.get(helper);
			double division = 2.0;
			
			if (it == null && wr == null) {
				finalTrust.put(helper, new FeedbackInfo(0.0,0));

			}
			else {
				if (it == null) {
					ro_IT = 0;
					trust_IT = 0;
					newcomerCount = 0;
					division = 1.0;
				}
				else {
					trust_IT = it.getRating();
					newcomerCount = it.getTotalRatings();
					
					if (it.getTotalRatings() < THRESHOLD_IT)
						ro_IT = it.getTotalRatings() / (double) THRESHOLD_IT;
					else 
						ro_IT = 1;
					}

				if (wr == null) {
					ro_WR = 0;
					trust_WR = 0;
					division = 1.0;
				} else {
					double sum = 0;
					double count = wr.size();
					for (Double d: wr) {
						sum += d;
					}
					trust_WR = sum / (double) count;

					if (count < THRESHOLD_W)
						ro_WR =  count / (double) THRESHOLD_W;
					else 
						ro_WR = 1.0;
				}
				
				trust = (trust_IT * ro_IT + trust_WR * ro_WR) / division;
				finalTrust.put(helper, new FeedbackInfo(trust,newcomerCount));
			}
		}

	
		
		ArrayList<AID> best = new ArrayList<AID>();
		ArrayList<AID> newcomers = new ArrayList<AID>();
		
		double max = -1;
		for (HashMap.Entry<AID, FeedbackInfo> entry : finalTrust.entrySet()) {
		    AID helper = entry.getKey();
		    FeedbackInfo trust = entry.getValue();
		    
		    if (trust.getRating() > max) {
		    	best.clear();
		    	best.add(helper);
		    	max = trust.getRating();
		    }else if (trust.getRating() == max) {
		    	best.add(helper);
		    }  
		    
		    if (trust.getTotalRatings() < NEWCOMER_THRESHOLD) {
		    	newcomers.add(helper);
		    }
		    
		    
		}
		best.addAll(newcomers);
		Set<AID> uniqueBest = new HashSet<AID>(best);
		
		AID selectedHelper = null;
		if(best.size() > 1){
			Random rand = new Random();
			int randomHelper = rand.nextInt(uniqueBest.size());
			selectedHelper = (AID) uniqueBest.toArray()[randomHelper];
			best.clear();
			//System.out.println("going to ask help to "+ best.getLocalName());
		}else
			selectedHelper = (AID) uniqueBest.toArray()[0];
		
		String list = "\n" ;
		
		for (HashMap.Entry<AID, FeedbackInfo> entry : finalTrust.entrySet()) {
			
			AID helper = entry.getKey();
			Double trustValue = entry.getValue().getRating();
			
			list += "[" + (int)helperInfo.get(helper).getRating() + "/" + helperInfo.get(helper).getTotalRatings() + "] ";
			list += helper.getLocalName() +": " ;
			if (trustValue >= 0) 
				list += " ";
			list += String.format("%.2f", trustValue);
			if(entry.getValue().getTotalRatings() < NEWCOMER_THRESHOLD)
				list += " [NC] ";
			if (selectedHelper == helper) 
				list += " <<<<<\n";
			else 
				list += "\n";
			
		}
		log.addToLog( list);
		
		//System.out.println("Escolhi o helper " + selectedHelper.getLocalName());
		return selectedHelper;
		
	}

	@Override
	//responder a um pedido de feedback relativamente a dada categoria
	public void answerFeedback(ACLMessage msg){
		String category = msg.getContent();
		HashMap<String, Object> sendInfo = new HashMap<String, Object>();

		for(int i = 0; i < helpers.size(); i++){

			AID helper = helpers.get(i);
			ArrayList<Double> helperRatings = interactionTrust.get(new HelperCategoryKey(helper, category));

			//se nao existe intera��o direta com o helper para aquela categoria
			if(helperRatings == null)
			{
				//System.out.println(getLocalName() + ": NAO TENHO INFORMACAO DO HELPER " + helper.getLocalName());
			}
			else{
				//System.out.println(getLocalName() + ": TENHO INFORMACAO DO HELPER " + helper.getLocalName() + ": " + helperRatings.size() + " RATINGS");			

				double mean = 0;
				for (Double a : helperRatings) {
					mean+= a;
				}
				mean /= (double) helperRatings.size();
				
				double trustRating = mean;
				if(helperRatings.size() < THRESHOLD_IT)
					trustRating *= trustRating / THRESHOLD_IT;
				sendInfo.put(helper.getLocalName(), new Double(trustRating));
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

	@Override
	protected void lastAnswerIs(boolean result) {
		double lastAnswer;
		
		if(result)
			log.addToLog("Correct Answer");
		else
			log.addToLog("Wrong Answer");
		
		if (result)
			lastAnswer = 1;
		else 
			lastAnswer = -1;
		
		HelperCategoryKey key = new HelperCategoryKey(lastHelper,lastCategory);
		ArrayList<Double> ratings = interactionTrust.get(key);
			
		if (ratings == null) {
			ratings = new ArrayList<Double>();
			interactionTrust.put(key, ratings);
		}
		ratings.add(lastAnswer);

		
		
	}
	
	protected void setup() {
		
		//formato: nome_variavel:valor_variavel
		//variaveis possiveis: IT, W, NC 
		//se nao forem especificadas, tomam o valor default
		Object[] args = getArguments();
		if(args != null && args.length > 0) {
			for(int i = 0; i < args.length; i++){
				String[] arg = ((String)args[i]).split(":");
				if(arg[0].equalsIgnoreCase("IT"))
					THRESHOLD_IT = Integer.parseInt(arg[1]);
				else if(arg[0].equalsIgnoreCase("W"))
					THRESHOLD_W = Integer.parseInt(arg[1]);
				if(arg[0].equalsIgnoreCase("NC"))
					NEWCOMER_THRESHOLD = Integer.parseInt(arg[1]);
				
			}
		}
		
		super.setup();
	}



}
