import jade.core.Agent;
import jade.core.behaviours.SimpleBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;

import java.util.ArrayList;
import java.util.Map;
import java.util.Random;

import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

// classe do agente
public class HelperAgent extends Agent {

	private QuestionsDatabase questions = new QuestionsDatabase();
	private HelperTrust knowledge = new HelperTrust();

	// classe do behaviour
	class HelperBehaviour extends SimpleBehaviour {

		// construtor do behaviour
		public HelperBehaviour(Agent a) {
			super(a);
		}

		// método action
		public void action() {

			ACLMessage msg = blockingReceive();
			if(msg.getPerformative() == ACLMessage.QUERY_REF) {
				System.out.println(getLocalName() + " - recebi " + msg.getContent());

				JSONParser parser=new JSONParser();
				Map obj;
				String question = null;
				ArrayList<String> options = null;
				try {
					obj = (Map)parser.parse(msg.getContent());
					
					question = (String)obj.get("question");
					options = (ArrayList<String>)obj.get("options");
				} catch (ParseException e) {
					e.printStackTrace();
				}
				
				String category = questions.getCategory(question);
				boolean answer = knowledge.getAnswer(category);
				
				// cria resposta
				ACLMessage reply = msg.createReply();
				// preenche conteúdo da mensagem
				if(answer){
					reply.setContent(questions.getCorrectAnswer(question));
				}
				else{
					//gerar numero aleatorio e escolher das options
					Random rand = new Random();
					int randomInt = rand.nextInt(options.size());
					reply.setContent(options.get(randomInt));
				}
				
				//System.out.println("HELPER SENT: " + reply.getContent());
				// envia mensagem
				reply.setPerformative(ACLMessage.INFORM);
				send(reply);
			}

		}

		// método done
		public boolean done() {
			return false;
		}

	}   // fim da classe PingPongBehaviour


	// método setup
	protected void setup() {
		
		knowledge.addCategory("desporto", 0.5);
		knowledge.addCategory("ciência", 0.1);
		knowledge.addCategory("literatura", 0.4);
		knowledge.addCategory("cultura geral", 0.2);

		// regista agente no DF
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());
		ServiceDescription sd = new ServiceDescription();
		sd.setName(getName());
		sd.setType("helper");
		dfd.addServices(sd);
		try {
			DFService.register(this, dfd);
		} catch(FIPAException e) {
			e.printStackTrace();
		}

		// cria behaviour
		HelperBehaviour b = new HelperBehaviour(this);
		addBehaviour(b);

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

}   // fim da classe PingPong

