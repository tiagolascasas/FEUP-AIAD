package smartSemaphores.jade.behaviours;

import java.util.Random;

import jade.lang.acl.ACLMessage;
import sajas.core.AID;
import sajas.core.behaviours.Behaviour;
import sajas.core.behaviours.CyclicBehaviour;
import smartSemaphores.jade.SemaphoreStates;
import smartSemaphores.jade.SemaphoricAgent;

public class RequestPerformerBehaviour extends CyclicBehaviour
{
	private static final long serialVersionUID = -4771134109565630310L;
	private static final String REQUEST_ID = "Request-priority";
	private static final String INFORM_ID = "Inform-priority";

	int step = 0;
	int accepted = 0;
	int repliesCnt = 0;
	private SemaphoricAgent thisAgent;

	@Override
	public void action()
	{
		thisAgent = (SemaphoricAgent) myAgent;
		thisAgent.increaseSecondsPassedOnState();
		switch (step)
		{
			case 0:
				System.out.println("RequestPerformer0");
				if (((SemaphoricAgent) myAgent).getCurrentState().equals( SemaphoreStates.GREEN))
				{
					return;
				}

				double priority = PriorityCalculator.calculatePriority(thisAgent);
				
				double ratio = priority / PriorityCalculator.EMERGENCY_PRIORITY; // 12 � o max priority

				Random generator = new Random(System.currentTimeMillis());

				if (generator.nextDouble() <= ratio)
				{
					
					ACLMessage requestMSG = new ACLMessage(ACLMessage.PROPOSE);

					for (int i = 0; i < ((SemaphoricAgent) myAgent).getNeighbours().size(); i++)
					{

						String name = ((SemaphoricAgent) myAgent).getNeighbours().get(i);
						requestMSG.addReceiver(new AID(name, AID.ISLOCALNAME));
					}

					requestMSG.setContent(Double.toString(priority));
					requestMSG.setConversationId(REQUEST_ID);
					myAgent.send(requestMSG);
					step = 1;
				} else
					step = 0;

				break;
			case 1:
				System.out.println("RequestPerformer1");
				ACLMessage reply = myAgent.receive();
				if (reply != null)
				{
					// Reply received
					if (reply.getPerformative() == ACLMessage.ACCEPT_PROPOSAL
							|| reply.getPerformative() == ACLMessage.REJECT_PROPOSAL)
					{
						repliesCnt++;
						if (reply.getPerformative() == ACLMessage.ACCEPT_PROPOSAL)
							accepted++;
					}

					if (repliesCnt >= ((SemaphoricAgent) myAgent).getNeighbours().size())
					{
						step = 2;
					}
				} else
				{
					block();
				}
				break;
			case 2:
				System.out.println("RequestPerformer2");
				ACLMessage informMSG;
				if (accepted >= repliesCnt)
				{
					informMSG = new ACLMessage(ACLMessage.CONFIRM);
					((SemaphoricAgent) myAgent).switchState(SemaphoreStates.GREEN);
				} else
					informMSG = new ACLMessage(ACLMessage.DISCONFIRM);

				for (int i = 0; i < ((SemaphoricAgent) myAgent).getNeighbours().size(); i++)
				{
					String name = ((SemaphoricAgent) myAgent).getNeighbours().get(i);
					informMSG.addReceiver(new AID(name, AID.ISLOCALNAME));
				}
				informMSG.setConversationId(INFORM_ID);
				myAgent.send(informMSG);
				step = 0;
				accepted = 0;
				repliesCnt = 0;
				break;
		}
	}

}
