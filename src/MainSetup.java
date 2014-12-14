import java.util.Scanner;

import jade.core.Agent;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;


@SuppressWarnings("serial")
public class MainSetup extends Agent{

	protected void setup() {
		ContainerController cc = getContainerController();
		try {
			cc.createNewAgent("Helper1", "HelperAgent", new Object[]{"desporto:75", "cultura:50"}).start();
			cc.createNewAgent("Helper2", "HelperAgent", new Object[]{"desporto:40", "cultura:50"}).start();
			cc.createNewAgent("Helper3", "HelperAgent", new Object[]{"desporto:60", "cultura:50"}).start();
			
			System.out.print("Waiting for helpers");
			Scanner s = new Scanner(System.in);
			s.nextLine();

			cc.createNewAgent("PlayerFIRE", "FIREPlayerAgent", null).start();
			cc.createNewAgent("PlayerSINALPHA", "SINALPHAPlayerAgent", null).start();
			cc.createNewAgent("PlayerBETA", "BETAPlayerAgent", null).start();
			
			System.out.print("Waiting for players/presenter");
			s.nextLine();
			s.close();
			
			cc.createNewAgent("Apresentador", "PresenterAgent", new Object[]{"7"}).start();
			
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
