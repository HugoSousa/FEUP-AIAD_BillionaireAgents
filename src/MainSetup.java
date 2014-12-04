import jade.core.Agent;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;


@SuppressWarnings("serial")
public class MainSetup extends Agent{

	protected void setup() {
		ContainerController cc = getContainerController();
		try {
			cc.createNewAgent("Helper1", "HelperAgent", null).start();
			cc.createNewAgent("Helper2", "HelperAgent", null).start();
			cc.createNewAgent("Helper3", "HelperAgent", null).start();
			
			cc.createNewAgent("Player1", "FIREPlayerAgent", null).start();
			cc.createNewAgent("Player2", "SINALPHAPlayerAgent", null).start();
			cc.createNewAgent("Player3", "BETAPlayerAgent", null).start();
			
			cc.createNewAgent("Apresentador", "PresenterAgent", null).start();
			
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
