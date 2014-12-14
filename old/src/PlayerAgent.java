import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.SimpleBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class PlayerAgent extends Agent{

	private int THRESHOLD = 10;
	private HashMap<AID, FIREPlayerTrust> interactionTrust = new HashMap<AID, FIREPlayerTrust>();
	private ArrayList<AID> players = new ArrayList<AID>();
	private AID selectedHelper;
	private String lastCategory;
	
	// classe do behaviour
	class PlayerBehaviour extends SimpleBehaviour {

		// construtor do behaviour
		public PlayerBehaviour(Agent a) {
			super(a);
		}

		// método action
		public void action() {

			ACLMessage msg = blockingReceive();
			//pode ser pergunta, resposta, ajuda
			if(msg.getPerformative() == ACLMessage.QUERY_REF) {
				//pergunta do apresentador

				//System.out.println(getLocalName() + "- recebi pergunta:" + msg.getContent());

				JSONParser parser=new JSONParser();
				Map obj;
				String category = null, question = null;
				ArrayList<String> answerOptions = null;
				try {
					obj = (Map)parser.parse(msg.getContent());

					category = (String)obj.get("category");
					question = (String)obj.get("question");
					answerOptions = (ArrayList<String>)obj.get("options");
					//System.out.println("OBJECT" + obj);
					//System.out.println("CATEGORIA - " + obj.get("category"));
				} catch (ParseException e) {
					e.printStackTrace();
				}

				//perguntar tambem aos outros participantes o que acham daquele helper nesta categoria
				//System.out.println("PERGUNTAR A OPINIAO A " + players.size() + " JOGADORES");
				ACLMessage playerMsg = new ACLMessage(ACLMessage.REQUEST);
				for(AID p: players)
					playerMsg.addReceiver(p);

				playerMsg.setContent(category);
				send(playerMsg);

				// criar array de ajudantes -> informacao
				HashMap<AID, FIREFeedbackInfo> certifiedReputation = new HashMap<AID, FIREFeedbackInfo> ();
				for (AID helperKey : interactionTrust.keySet()) {
					certifiedReputation.put(helperKey, new FIREFeedbackInfo(0, 0));
				} // os que tiverem 0 totalanswers no fim devem ser removidos

				//bloquear o numero de vezes igual ao numero de players?
				for(int i=0; i < players.size(); i++){
					ACLMessage playerFeedbackMsg = blockingReceive();

					//System.out.println("RECEBI FEEDBACK PLAYER" + playerFeedbackMsg.getContent());

					try {
						obj = (Map)parser.parse(playerFeedbackMsg.getContent());

						// percorrer cada helper recebido
						for (Object key : obj.keySet()) {
							String helperName = (String)key;

							String[] values = ((String)obj.get(key)).split("/"); 
							int positiveAnswers = Integer.parseInt(values[0]);
							int totalAnswers =  Integer.parseInt(values[1]);
							//System.out.println("AHHH " + helperName + " " + positiveAnswers + " / " + totalAnswers);

							for (AID helperKey : certifiedReputation.keySet()) {
								if (helperKey.getLocalName().equals(helperName)) {
									certifiedReputation.get(helperKey).addPositiveAnswers(positiveAnswers);
									certifiedReputation.get(helperKey).addTotalAnswers(totalAnswers);
									if (totalAnswers > 0) certifiedReputation.get(helperKey).addFeedback();
								};
							} 


						}
					} catch (ParseException e) {
						e.printStackTrace();
					}
				}


				 selectedHelper = getBestHelper(category, certifiedReputation);
				 lastCategory = category;

				//perguntar ao ajudante
				ACLMessage helpMsg = new ACLMessage(ACLMessage.QUERY_REF);
				helpMsg.addReceiver(selectedHelper);

				JSONObject obj2 = new JSONObject();
				obj2.put("question", question);
				obj2.put("options", answerOptions);

				StringWriter out = new StringWriter();
				try {
					obj2.writeJSONString(out);
				} catch (IOException e1) {
					e1.printStackTrace();
				}		

				//enviar questão e respostas possiveis
				helpMsg.setContent(out.toString());
				send(helpMsg);

				ACLMessage helperAnswer = blockingReceive();
				//if(helperAnswer.getPerformative() == ACLMessage.INFORM)
					//System.out.println(getLocalName() + " - recebi " + helperAnswer.getContent());
				//System.out.println("SENDING TO HELPER " + selectedHelper.getLocalName() + ":" + helpMsg.getContent());



				//enviar resposta
				ACLMessage reply = msg.createReply();
				reply.setPerformative(ACLMessage.INFORM_REF);
				reply.setContent(helperAnswer.getContent());
				send(reply);


			}
			else if(msg.getPerformative() == ACLMessage.INFORM_REF){
				//informação da resposta correta
				//System.out.println(msg.getContent());
				if (msg.getContent().equals("Correct"))
					interactionTrust.get(selectedHelper).addNewAnswer(lastCategory,true);
				else 
					interactionTrust.get(selectedHelper).addNewAnswer(lastCategory,false);

				

				//comparar se respondi bem/mal, fazer calculos necessarios relativamente a confiança dos helpers

			}
			else if(msg.getPerformative() == ACLMessage.REQUEST){
				//feedback de um player
				String category = msg.getContent();

				HashMap<AID, FIREFeedbackInfo> feedback = new HashMap<AID, FIREFeedbackInfo>();
				for (AID key : interactionTrust.keySet()) {
					FIREFeedbackInfo ffi = interactionTrust.get(key).getInfoByCategory(category);
					if(ffi != null)
						feedback.put(key, ffi);
					else
						feedback.put(key, new FIREFeedbackInfo(0,0));
				}
				ACLMessage feedbackMsg = msg.createReply();
				feedbackMsg.setPerformative(ACLMessage.INFORM);				

				JSONObject obj = new JSONObject();
				for (AID key : feedback.keySet()) {
					obj.put(key.getLocalName(), feedback.get(key).toString());
				}

				StringWriter out = new StringWriter();
				try {
					obj.writeJSONString(out);
				} catch (IOException e1) {
					e1.printStackTrace();
				}

				//System.out.println("HEY" + out.toString());
				feedbackMsg.setContent(out.toString());
				send(feedbackMsg);

			}

		}

		private AID getBestHelper(String category, HashMap<AID, FIREFeedbackInfo> certifiedReputation) {


			HashMap<AID, Double> trustByHelper = getTrustByHelper(category, certifiedReputation);

			//COMBINAR A AJUDA DOS OUTROS PLAYERS E A PROPRIA CONFIANÇA
			//VER QUAL O MELHOR

			ArrayList<AID> best = new ArrayList<AID>();
			Double maxTrustHelper = new Double(-1.0);
			for (AID key : trustByHelper.keySet()) {
				Double trustHelper = trustByHelper.get(key);

				if(trustHelper.doubleValue() > maxTrustHelper.doubleValue()){
					maxTrustHelper = trustHelper;
					best.clear();
					best.add(key);
				}else if(trustHelper.doubleValue() == maxTrustHelper.doubleValue()){
					best.add(key);
				}
			}

			//System.out.println("E POSSIVEL ESCOLHER ENTRE " + best.size() + " HELPERS");
			//se ha mais que um helper com mesma pontuação, escolher um aleatorio
			AID selectedHelper = null;
			if(best.size() > 1){
				Random rand = new Random();
				int randomHelper = rand.nextInt(best.size());
				selectedHelper = best.get(randomHelper);

				best.clear();
				//System.out.println("going to ask help to "+ best.getLocalName());
			}else
				selectedHelper = best.get(0);
			return selectedHelper;
		}

		private HashMap<AID, Double> getTrustByHelper(String category, HashMap<AID, FIREFeedbackInfo> certifiedReputation) {
			HashMap<AID, Double> trustByHelper = new  HashMap<AID, Double>();

			for (AID helperKey : interactionTrust.keySet()) {

				double trust, roIT, roCR, wIT, wCR;


				int tAnswersIT = interactionTrust.get(helperKey).getInfoByCategory(category).getTotalAnswers();
				int pAnswersIT = interactionTrust.get(helperKey).getInfoByCategory(category).getPositiveAnswers();
				int nAnswersIT = tAnswersIT - pAnswersIT;

				if (tAnswersIT == 0) {
					wIT = 0;
					roIT = 1;
					}
				else {
					double weightPerAnswerIT = 1.0/(double)tAnswersIT;

					if(tAnswersIT > (double)THRESHOLD)
						roIT = 1;
					else
						roIT = tAnswersIT/(double)THRESHOLD;

					wIT = weightPerAnswerIT * pAnswersIT - weightPerAnswerIT * nAnswersIT;
				}

				int tAnswersCR = certifiedReputation.get(helperKey).getTotalAnswers();
				int pAnswersCR = certifiedReputation.get(helperKey).getPositiveAnswers();
				int nAnswersCR = tAnswersCR - pAnswersCR;
				
				if (tAnswersCR == 0) {
					wCR = 0;
					roCR = 1;
				}
				else {
				
				double weightPerAnswerCR = 1.0/(double)tAnswersCR;
				int feedbacksCR = certifiedReputation.get(helperKey).getFeedbacks();


				if(tAnswersCR > (double)THRESHOLD * feedbacksCR)
					roCR = 1;
				else
					roCR = tAnswersCR/((double)THRESHOLD * feedbacksCR);

				wCR = weightPerAnswerCR * pAnswersCR - weightPerAnswerCR * nAnswersCR;
				
				}

				if (roIT + roCR == 0.0) 
					trust = 0;
				else 
					trust = (wIT * roIT + wCR * roCR) / (roIT + roCR);

				//System.out.println("AGENTE-TRUST" + helperKey.getLocalName() + " -> " + trust);
				trustByHelper.put(helperKey, new Double(trust));
			}
			return trustByHelper;
		}

		// método done
		public boolean done() {
			return false;
		}

	}   // fim da classe PingPongBehaviour


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
		PlayerBehaviour b = new PlayerBehaviour(this);
		addBehaviour(b);

		//encontrar os ajudantes

		DFAgentDescription template = new DFAgentDescription();
		ServiceDescription sd1 = new ServiceDescription();

		sd1.setType("helper");
		template.addServices(sd1);

		try {
			DFAgentDescription[] result = DFService.search(this, template);
			//AID[] agents = new AID[result.length];
			for (int i=0; i<result.length; i++){
				interactionTrust.put(result[i].getName(), new FIREPlayerTrust());
				//System.out.println("HELPER " + i + " - " + result[i].getName());
			}


		} catch(FIPAException e) { e.printStackTrace(); }


		//encontrar os outros jogadores		
		sd1.setType("player");
		template.addServices(sd1);
		try {
			DFAgentDescription[] result = DFService.search(this, template);
			//AID[] agents = new AID[result.length];
			for (int i=0; i<result.length; i++){

				if(!result[i].getName().equals(getAID())){
					players.add(result[i].getName());
				}
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

}
