import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.SimpleBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;

import java.util.ArrayList;
import java.util.HashMap;


@SuppressWarnings("serial")
public abstract class GenericPlayerAgent extends Agent{

	protected ArrayList<AID> players = new ArrayList<AID>();
	protected ArrayList<AID> helpers = new ArrayList<AID>();

	// classe do behaviour
	class GenericBehaviour extends SimpleBehaviour {

		// construtor do behaviour
		public GenericBehaviour(Agent a) {
			super(a);
		}

		// método action
		public void action() {

			ACLMessage presenterQuestionMsg = blockingReceive();
			if(presenterQuestionMsg.getPerformative() == ACLMessage.QUERY_REF) {
				//pergunta do apresentador
				String content = presenterQuestionMsg.getContent();
				HashMap<String, Object> questionFields = Utils.JSONDecode(content);

				String category = (String) questionFields.get("category");
				String question = (String) questionFields.get("question");
				@SuppressWarnings("unchecked")
				ArrayList<String> answerOptions = (ArrayList<String>) questionFields.get("options");

				System.out.println("CATEGORY: " + category);

				System.out.println(getLocalName() + "- recebi pergunta:" + content);

				String answer = processQuestion(category, question, answerOptions );
				
				ACLMessage replyAnswer = presenterQuestionMsg.createReply();
				replyAnswer.setPerformative(ACLMessage.INFORM_REF);
				replyAnswer.setContent(answer);
				send(replyAnswer);
			
			}
			else if(presenterQuestionMsg.getPerformative() == ACLMessage.INFORM_REF){
				//informação da resposta correta
				//comparar se respondi bem/mal, recalcular o rating e adicionar no hashmap
				if(presenterQuestionMsg.getContent().equals("Correct")){
					//mensagem correta
					System.out.println(getLocalName() + ": My answer was correct");
					lastAnswerIs(true);
				}else if(presenterQuestionMsg.getContent().equals("Wrong")){
					lastAnswerIs(false);
					System.out.println(getLocalName() + ": My answer was wrong");
				}
				
			}
			else if(presenterQuestionMsg.getPerformative() == ACLMessage.REQUEST){
				//feedback de um player
				answerFeedback(presenterQuestionMsg);
			}
		}
		

		// método done
		public boolean done() {
			return false;
		}

	}  


	// método setup
	protected void setup() {

		// regista agente no DF

		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());
		ServiceDescription sd = new ServiceDescription();
		sd.setName(getName());
		sd.setType("player");
		dfd.addServices(sd);
		try {
			DFService.register(this, dfd);
		} catch(FIPAException e) {
			e.printStackTrace();
		}

		// cria behaviour
		GenericBehaviour b = new GenericBehaviour(this);
		addBehaviour(b);

		DFAgentDescription template = new DFAgentDescription();
		ServiceDescription sd1 = new ServiceDescription();
		
		//encontrar os outros jogadores		
		sd1.setType("player");
		template.addServices(sd1);
		try {
			DFAgentDescription[] result = DFService.search(this, template);
			
			for (int i=0; i<result.length; i++){

				if(!result[i].getName().equals(getAID())){
					players.add(result[i].getName());
				}
			}

		} catch(FIPAException e) { e.printStackTrace(); }
		
		sd1.setType("helper");
		template.addServices(sd1);

		try {
			DFAgentDescription[] result = DFService.search(this, template);
			
			for (int i=0; i<result.length; i++){
				helpers.add(result[i].getName());
			}


		} catch(FIPAException e) { e.printStackTrace(); }

	}   // fim do metodo setup

	// método takeDown
	protected void takeDown() {
		// retira registo no DF

		try {
			DFService.deregister(this);  
		} catch(FIPAException e) {
			e.printStackTrace();
		}

	}

	protected abstract String processQuestion(String category, String question, ArrayList<String> answerOptions);
	protected abstract void lastAnswerIs(boolean result) ;

	protected HashMap<AID, ArrayList<Double>> askOtherPlayers(String category) {
		ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
		for(AID p: players)
			msg.addReceiver(p);

		//System.out.println("SENT CATEGORY " + category + " TO OTHER PLAYERS");
		msg.setContent(category);
		send(msg);
	
		//valor de confiança de interação para cada helper na dada confiança
		HashMap<AID, ArrayList<Double>> HelperRatingsByCategory = new HashMap<AID, ArrayList<Double>>();

		for(int i=0; i < players.size(); i++){
			//recebe feedback de outro player
			ACLMessage playerFeedbackMsg = blockingReceive();
			processPlayerFeedback(playerFeedbackMsg, HelperRatingsByCategory);
		}
		return HelperRatingsByCategory;
		
	}
	
	protected  void processPlayerFeedback(ACLMessage playerFeedbackMsg, HashMap<AID, ArrayList<Double>> helperRatingsByCategory) {
		
		String content = playerFeedbackMsg.getContent();

		if(content.equals("none")){
			System.out.println(playerFeedbackMsg.getSender().getLocalName() + ": NAO TEM INFO DE NENHUM HELPER");
		}
		else{
			//formato: helpername: rating / respostas
			//TODO
			//FAZER CALCULOS DA INTERAÇAO COM OS OUTROS JOGADORES
			HashMap<String, Object> helperRatingsFromPlayer= Utils.JSONDecode(content);
			
			for (String helperName: helperRatingsFromPlayer.keySet()) {
				Double rating = (Double) helperRatingsFromPlayer.get(helperName);
				System.out.println("PARA O  " + playerFeedbackMsg.getSender().getLocalName() +", O HELPER " + helperName + " TEM O RATING DE " + rating);
				
				AID helperAID = getHelperAID(helperName);
				
				ArrayList<Double> helperRatings = helperRatingsByCategory.get(helperAID);
				
				//se nao existe informacao deste helper ainda
				if(helperRatings == null){
					helperRatings = new ArrayList<Double>();
				}
				
				helperRatings.add(rating);
				
			}
			
		}
		
	}

	protected void answerFeedback(ACLMessage msg) {}

	protected String askHelper(AID selectedHelper, String question, ArrayList<String> answerOptions) {
	HashMap<String, Object> sendInfo = new HashMap<String, Object>();
		
		sendInfo.put("question", question);
		sendInfo.put("options", answerOptions);
		
		String out = Utils.JSONEncode(sendInfo);
		
		ACLMessage helpMsg = new ACLMessage(ACLMessage.QUERY_REF);
		helpMsg.addReceiver(selectedHelper);
		helpMsg.setContent(out);
		send(helpMsg);
		
		ACLMessage helperAnswer = blockingReceive();
		if(helperAnswer.getPerformative() == ACLMessage.INFORM){
			System.out.println(getLocalName() + " - recebi " + helperAnswer.getContent());
			return helperAnswer.getContent();
		}
		return null;
	}
	
	private AID getHelperAID(String helperName){
		for(AID helper: helpers){
			if(helper.getLocalName().equals(helperName))
				return helper;
		}
		
		return null;
	}
}

