import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;


public class MainSetup extends Agent{

	protected void setup() {
		ContainerController cc = getContainerController();
		AgentController ac;
		try {
			ac = cc.createNewAgent("Helper1", "HelperAgent", null);
			ac.start();
			ac = cc.createNewAgent("Helper2", "HelperAgent", null);
			ac.start();
			ac = cc.createNewAgent("Helper3", "HelperAgent", null);
			ac.start();
			
			ac = cc.createNewAgent("Player1", "PlayerAgent", null);
			ac.start();
			ac = cc.createNewAgent("Player2", "PlayerAgent", null);
			ac.start();
			ac = cc.createNewAgent("Player3", "PlayerAgent", null);
			ac.start();
			
			ac = cc.createNewAgent("Apresentador", "PresenterAgent", null);
			ac.start();
			
		} catch (StaleProxyException e) {
			e.printStackTrace();
		}
		
		//kill this agent
		doDelete();

	}

	protected void takeDown() {
		//com o doDelete, o deregister da erro
		// retira registo no DF
		/*
		try {
			//DFService.deregister(this);  
		} catch(FIPAException e) {
			e.printStackTrace();
		}
		*/
	}
}
