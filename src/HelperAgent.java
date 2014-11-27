import jade.core.Agent;
import jade.core.behaviours.SimpleBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;

//import java.util.ArrayList;
import java.util.HashMap;

// classe do agente
@SuppressWarnings("serial")
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

				answerPlayer(msg);
			}

		}

		private void answerPlayer(ACLMessage msg) {
			HashMap<String, Object> fields = Utils.JSONDecode(msg.getContent());
			String question = (String) fields.get("question");
			//@SuppressWarnings("unchecked")
			//ArrayList<String> options = (ArrayList<String>)fields.get("options");
			
			String category = questions.getCategory(question);
			boolean answer = knowledge.getAnswer(category);
			
			ACLMessage reply = msg.createReply();

			if(answer){
				reply.setContent(questions.getCorrectAnswer(question));
			}
			else{
				reply.setContent(questions.getWrongAnswer(question));
			}
			
			//System.out.println("HELPER SENT: " + reply.getContent());
			// envia mensagem
			reply.setPerformative(ACLMessage.INFORM);
			send(reply);
		}

		// método done
		public boolean done() {
			return false;
		}

	}


	// método setup
	protected void setup() {
		
		knowledge.addCategory("desporto",0.7);
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

