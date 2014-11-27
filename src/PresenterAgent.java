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
import java.util.Random;


@SuppressWarnings("serial")
public class PresenterAgent extends Agent{

	private QuestionsDatabase questions = new QuestionsDatabase();
	private ArrayList<AID> players = new ArrayList<AID>();
	private int playerTurn = 0;
	private Question actualQuestion = null;
	
	// classe do behaviour
	class PresenterBehaviour extends SimpleBehaviour {
		
		private int round = 0;

		// construtor do behaviour
		public PresenterBehaviour(Agent a) {
			super(a);
		}

		// método action
		public void action() {
			ACLMessage msg = new ACLMessage(ACLMessage.QUERY_REF);
			msg.addReceiver(players.get(playerTurn));
			
			//escolher pergunta aleatoria
			Random rand = new Random();
		    int randomQuestion = rand.nextInt(questions.size());
		    actualQuestion = questions.get(randomQuestion);
		    //questions.remove(actualQuestion);
		    
		    HashMap<String, Object> question = new HashMap<String, Object>();
		    question.put("question", actualQuestion.getText());
		    question.put("category", actualQuestion.getCategory());
		    question.put("difficulty", new Integer(actualQuestion.getDifficulty()));
		    question.put("options", actualQuestion.getAnswerOptions());
		    String out = Utils.JSONEncode(question);
		    
		    
		    //System.out.println("PRESENTER SENDING " + out.toString());
		    
			msg.setContent(out);
			send(msg);
			
			ACLMessage receiveMsg = blockingReceive();
			if(receiveMsg.getPerformative() == ACLMessage.INFORM_REF) {
			
				System.out.println("verificar player");
				
				//verificar se a resposta veio do player correto
				if(receiveMsg.getSender().equals(players.get(playerTurn))){
					String playerAnswer = receiveMsg.getContent();
					System.out.println(getLocalName() + " - recebi resposta " + playerAnswer);
					
					
					ACLMessage replyAnswer = receiveMsg.createReply();
		            replyAnswer.setPerformative(ACLMessage.INFORM_REF);
					//responder certo/errado 
		            
					if(playerAnswer.equals(actualQuestion.getCorrectAnswer())){
						replyAnswer.setContent("Correct");
					}
					else{
						replyAnswer.setContent("Wrong");  
					}

					//System.out.println("PRESENTER SENDING: " + replyAnswer.getContent());
					send(replyAnswer);
					
					round++;
					nextPlayer();
					//enviar pergunta ao proximo concorrente
				}
						
			}
		}

		// método done
		public boolean done() {
			return round == 50;
		}

	}   // fim da classe PingPongBehaviour


	// método setup
	protected void setup() {		
		
		// regista agente no DF
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());
		ServiceDescription sd = new ServiceDescription();
		sd.setName(getName());
		sd.setType("presenter");
		dfd.addServices(sd);
		try {
			DFService.register(this, dfd);
		} catch(FIPAException e) {
			e.printStackTrace();
		}

		// cria behaviour
		PresenterBehaviour b = new PresenterBehaviour(this);
		addBehaviour(b);
		
		
		//procura por agentes PlayerAgent
		DFAgentDescription template = new DFAgentDescription();
		ServiceDescription sd1 = new ServiceDescription();
		
		sd1.setType("player");
		template.addServices(sd1);
		
		try {
			DFAgentDescription[] result = DFService.search(this, template);
			for (int i=0; i<result.length; i++){
				System.out.println("PLAYER " + i + " - " + result[i].getName());
				players.add(result[i].getName());
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
	
	private void nextPlayer(){
		if(playerTurn < players.size() - 1)
			playerTurn++;
		else
			playerTurn = 0;
	}
}
