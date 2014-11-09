import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import jade.core.*;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.DFService;
import jade.domain.FIPAException;

public class PlayerAgent extends Agent{

	private HashMap<AID, FIREPlayerTrust> trust = new HashMap<AID, FIREPlayerTrust>();
	private ArrayList<AID> players = new ArrayList<AID>();

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
				
				System.out.println(getLocalName() + "- recebi pergunta:" + msg.getContent());
				
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
				System.out.println("PERGUNTAR A OPINIAO A " + players.size() + " JOGADORES");
				ACLMessage playerMsg = new ACLMessage(ACLMessage.REQUEST);
				for(AID p: players)
					playerMsg.addReceiver(p);
			    
			    playerMsg.setContent(category);
				send(playerMsg);
				
				//bloquear o numero de vezes igual ao numero de players?
				for(int i=0; i < players.size(); i++){
					ACLMessage playerFeedbackMsg = blockingReceive();
					
					System.out.println("RECEBI FEEDBACK PLAYER");
					System.out.println(playerFeedbackMsg.getContent());
					
					try {
						obj = (Map)parser.parse(playerFeedbackMsg.getContent());
						
						for (Object key : obj.keySet()) {
						    String helperName = (String)key;
						    
						    String[] values = ((String)obj.get(key)).split("/"); 
						    int positiveAnswers = Integer.parseInt(values[0]);
						    int totalAnswers =  Integer.parseInt(values[1]);
						    System.out.println("AHHH " + helperName + " " + positiveAnswers + " / " + totalAnswers);
							
						    //devia usar-se o AID diretamente
						    for (AID helperKey : trust.keySet()) {
							    if(helperKey.getLocalName().equals(helperName)){
							    	FIREFeedbackInfo combineFeedback = trust.get(helperKey).getInfoByCategory(category);
							    	System.out.println("COMBINE FEEDBACK " + combineFeedback);
							    	if(combineFeedback == null && totalAnswers > 0){
							    		FIREFeedbackInfo newInfo = new FIREFeedbackInfo(positiveAnswers, totalAnswers);
							    		FIREPlayerTrust newTrust = new FIREPlayerTrust();
							    		newTrust.addCategory(category, newInfo);
							    	}
							    	else if(combineFeedback != null){
							    		combineFeedback.setPositiveAnswers(combineFeedback.getPositiveAnswers() + positiveAnswers);
							    		combineFeedback.setTotalAnswers(combineFeedback.getTotalAnswers() + totalAnswers);
							    	}
							    }
							}
						}
					} catch (ParseException e) {
						e.printStackTrace();
					}
				}
				
				
				//COMBINAR A AJUDA DOS OUTROS PLAYERS E A PROPRIA CONFIANÇA
				//VER QUAL O MELHOR
				
				ArrayList<AID> best = new ArrayList<AID>();
				for (AID key : trust.keySet()) {
				    Double maxTrustHelper = new Double(-1.0);
				    
				    Double trustHelper = trust.get(key).getTrustByCategory(category);
				    if(trustHelper == null){
				    	trustHelper = new Double(0.0);
				    }
				    if(trustHelper.doubleValue() >= maxTrustHelper.doubleValue()){
			    		maxTrustHelper = trustHelper;
			    		best.add(key);
			    	}
				}
				
				
				System.out.println("E POSSIVEL ESCOLHER ENTRE " + best.size() + " HELPERS");
				//se ha mais que um helper com mesma pontuação, escolher um aleatorio
				Random rand = new Random();
				int randomHelper = rand.nextInt(best.size());
				AID selectedHelper = best.get(randomHelper);
				
				best.clear();
				//System.out.println("going to ask help to "+ best.getLocalName());
				
		
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
				if(helperAnswer.getPerformative() == ACLMessage.INFORM)
				System.out.println(getLocalName() + " - recebi " + helperAnswer.getContent());
				//System.out.println("SENDING TO HELPER " + selectedHelper.getLocalName() + ":" + helpMsg.getContent());
				
				
				
				//enviar resposta
				ACLMessage reply = msg.createReply();
				reply.setPerformative(ACLMessage.INFORM_REF);
				reply.setContent(helperAnswer.getContent());
				send(reply);
				

			}
			else if(msg.getPerformative() == ACLMessage.INFORM_REF){
				//informação da resposta correta
				System.out.println(getLocalName() + ": resposta certa - " + msg.getContent());

				//comparar se respondi bem/mal, fazer calculos necessarios relativamente a confiança dos helpers

			}
			else if(msg.getPerformative() == ACLMessage.REQUEST){
				//feedback de um player
				String category = msg.getContent();
				
				HashMap<AID, FIREFeedbackInfo> feedback = new HashMap<AID, FIREFeedbackInfo>();
				for (AID key : trust.keySet()) {
					FIREFeedbackInfo ffi = trust.get(key).getInfoByCategory(category);
					if(ffi != null)
						feedback.put(key, ffi);
					else
						feedback.put(key, new FIREFeedbackInfo(0,0));
				}
				System.out.println("AHHHHHHHHHHHHHH VOU ENVIAR " + feedback);
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
			    
			    System.out.println("HEY" + out.toString());
			    feedbackMsg.setContent(out.toString());
				send(feedbackMsg);
				
			}

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
				trust.put(result[i].getName(), new FIREPlayerTrust());
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
