package smartSemaphores;

import java.awt.Point;
import java.util.ArrayList;

import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.wrapper.StaleProxyException;
import repast.simphony.context.Context;
import repast.simphony.context.space.graph.NetworkBuilder;
import repast.simphony.context.space.grid.GridFactory;
import repast.simphony.context.space.grid.GridFactoryFinder;
import repast.simphony.dataLoader.ContextBuilder;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridBuilderParameters;
import repast.simphony.space.grid.SimpleGridAdder;
import repast.simphony.space.grid.StickyBorders;
import sajas.core.Agent;
import sajas.core.Runtime;
import sajas.sim.repasts.RepastSLauncher;
import sajas.wrapper.ContainerController;
import smartSemaphores.jade.ConsensualSemaphoricAgent;
import smartSemaphores.jade.RoadAgent;
import smartSemaphores.jade.SemaphoricAgent;
import smartSemaphores.jade.SinkAgent;
import smartSemaphores.jade.SmartSemaphoricAgent;
import smartSemaphores.jade.TimedSemaphoricAgent;
import smartSemaphores.repast.SimulationManager;

public class SmartSemaphores extends RepastSLauncher implements ContextBuilder<Object>
{
	// Constants
	public static final float TICKS_PER_SECOND = 1.0f;

	// Configurable simulation variables and definitions
	public static SimulationType simulationType = SimulationType.SMART_AGENTS;
	public static int HOURS = 5;
	public static int EXIT_RATE = 3;
	public static int MAX_TICKS;
	public static int MIN_STARTING_CARS = 100;
	public static int MAX_STARTING_CARS = 800;
	public static double EMERGENCY_PROBABILITY = 0.001f;
	public static double PEDESTRIAN_PROBABILITY = 0.15f;

	// JADE containers and agents
	@SuppressWarnings("unused")
	private ContainerController mainContainer;
	private ContainerController crossContainerA;
	private ContainerController crossContainerB;
	private ContainerController crossContainerC;
	private ArrayList<RoadAgent> agents;

	// Simulation manager (repast agent)
	private SimulationManager manager;

	// Grid
	private Grid<Object> grid;

	// Context
	private Context<Object> context;

	@Override
	public String getName()
	{
		return "SmartSemaphores";
	}

	@Override
	protected void launchJADE()
	{
		Runtime rt = Runtime.instance();
		Profile p0 = new ProfileImpl();
		Profile p1 = new ProfileImpl();
		Profile p2 = new ProfileImpl();
		Profile p3 = new ProfileImpl();

		this.mainContainer = rt.createMainContainer(p0);
		this.crossContainerA = rt.createAgentContainer(p1);
		this.crossContainerB = rt.createAgentContainer(p2);
		this.crossContainerC = rt.createAgentContainer(p3);

		this.agents = new ArrayList<>();

		switch (SmartSemaphores.simulationType)
		{
			case SMART_AGENTS:
				launchSmartAgents(true);
				break;
			case CONSENSUAL_AGENTS:
				launchSmartAgents(false);
				break;
			case TIMED_AGENTS:
				launchTimedAgents();
				break;
		}

		int[] sources = { 1, 8, 10, 6, 15, 17 };
		int[] middles = { 3, 4, 11, 12, 13, 14 };
		int[] sinks = { 2, 7, 9, 5, 16, 18 };
		this.manager.init(sources, middles, sinks);
	}

	private void launchSmartAgents(boolean smart)
	{
		try
		{
			// Semaphoric agents on road cross A
			int[] crossAagents = { 1, 11, 4, 8 };
			int[] crossAconnectables = { 2, 7, 12, 3 };
			for (int i : crossAagents)
			{
				SemaphoricAgent agent;
				if (smart)
					agent = new SmartSemaphoricAgent(i, crossAagents, crossAconnectables, 1000);
				else
					agent = new ConsensualSemaphoricAgent(i, crossAagents, crossAconnectables, 1000);
				this.crossContainerA.acceptNewAgent("Agent " + i, agent).start();
				this.agents.add(agent);

				putOnGrid(i, agent);

			}

			// Semaphoric agents on road cross B
			int[] crossBagents = { 3, 13, 6, 10 };
			int[] crossBconnectables = { 5, 9, 4, 14 };
			for (int i : crossBagents)
			{
				SemaphoricAgent agent;
				if (smart)
					agent = new SmartSemaphoricAgent(i, crossBagents, crossBconnectables, 1000);
				else
					agent = new ConsensualSemaphoricAgent(i, crossBagents, crossBconnectables, 1000);
				this.crossContainerB.acceptNewAgent("Agent " + i, agent).start();
				this.agents.add(agent);

				putOnGrid(i, agent);
			}

			// Semaphoric agents on road cross C
			int[] crossCagents = { 14, 17, 15, 12 };
			int[] crossCconnectables = { 18, 16, 11, 13 };
			for (int i : crossCagents)
			{
				SemaphoricAgent agent;
				if (smart)
					agent = new SmartSemaphoricAgent(i, crossCagents, crossCconnectables, 1000);
				else
					agent = new ConsensualSemaphoricAgent(i, crossCagents, crossCconnectables, 1000);
				this.crossContainerC.acceptNewAgent("Agent " + i, agent).start();
				this.agents.add(agent);

				putOnGrid(i, agent);
			}

			initSinkAgents();

		} catch (StaleProxyException e)
		{
			e.printStackTrace();
		}
	}

	private void launchTimedAgents()
	{

		try
		{
			// Semaphoric agents on road cross A
			int[] crossAagents = { 1, 11, 4, 8 };
			int[] crossAconnectables = { 2, 7, 12, 3 };
			int seq = 1;
			for (int i : crossAagents)
			{
				TimedSemaphoricAgent agent = new TimedSemaphoricAgent(i, crossAagents, crossAconnectables, 1000, seq);
				this.crossContainerA.acceptNewAgent("Agent " + i, agent).start();
				this.agents.add(agent);
				seq++;

				putOnGrid(i, agent);
			}

			// Semaphoric agents on road cross B
			int[] crossBagents = { 3, 13, 6, 10 };
			int[] crossBconnectables = { 5, 9, 4, 14 };
			seq = 1;
			for (int i : crossBagents)
			{
				TimedSemaphoricAgent agent = new TimedSemaphoricAgent(i, crossBagents, crossBconnectables, 1000, seq);
				this.crossContainerB.acceptNewAgent("Agent " + i, agent).start();
				this.agents.add(agent);
				seq++;

				putOnGrid(i, agent);
			}

			// Semaphoric agents on road cross C
			int[] crossCagents = { 14, 17, 15, 12 };
			int[] crossCconnectables = { 18, 16, 11, 13 };
			seq = 1;
			for (int i : crossCagents)
			{
				TimedSemaphoricAgent agent = new TimedSemaphoricAgent(i, crossCagents, crossCconnectables, 1000, seq);
				this.crossContainerC.acceptNewAgent("Agent " + i, agent).start();
				this.agents.add(agent);
				seq++;

				putOnGrid(i, agent);
			}

			initSinkAgents();

		} catch (StaleProxyException e)
		{
			e.printStackTrace();
		}
	}

	private void initSinkAgents() throws StaleProxyException
	{
		// Sink Agents (two sinks per cross)
		SinkAgent sinkAgent;

		sinkAgent = new SinkAgent(2);
		this.crossContainerA.acceptNewAgent("Agent 2", sinkAgent).start();
		this.agents.add(sinkAgent);
		putOnGrid(2, sinkAgent);

		sinkAgent = new SinkAgent(7);
		this.crossContainerA.acceptNewAgent("Agent 7", sinkAgent).start();
		this.agents.add(sinkAgent);
		putOnGrid(7, sinkAgent);

		sinkAgent = new SinkAgent(5);
		this.crossContainerB.acceptNewAgent("Agent 5", sinkAgent).start();
		this.agents.add(sinkAgent);
		putOnGrid(5, sinkAgent);

		sinkAgent = new SinkAgent(9);
		this.crossContainerB.acceptNewAgent("Agent 9", sinkAgent).start();
		this.agents.add(sinkAgent);
		putOnGrid(9, sinkAgent);

		sinkAgent = new SinkAgent(16);
		this.crossContainerC.acceptNewAgent("Agent 16", sinkAgent).start();
		this.agents.add(sinkAgent);
		putOnGrid(16, sinkAgent);

		sinkAgent = new SinkAgent(18);
		this.crossContainerC.acceptNewAgent("Agent 18", sinkAgent).start();
		this.agents.add(sinkAgent);
		putOnGrid(18, sinkAgent);
	}

	@Override
	public Context<?> build(Context<Object> context)
	{
		this.context = context;

		initSimulation();
		
		GridFactory gridFactory = GridFactoryFinder.createGridFactory(null);

		// implement strict borders so there is no wrapping
		// multiple occupancy of grid cells is still set to false
		this.grid = gridFactory.createGrid("SmartSemaphores Road Grid", context,
				new GridBuilderParameters<Object>(new StickyBorders(), new SimpleGridAdder<Object>(), false, 15, 19));

		NetworkBuilder<Object> netBuilder = new NetworkBuilder<Object>("SmartSemaphores Road Network", context, true);
		netBuilder.buildNetwork();
		
		this.manager = new SimulationManager(this);
		context.add(this.manager);

		return super.build(context);
	}

	private void initSimulation()
	{
		// Parameters parm = RunEnvironment.getInstance().getParameters();
		// numAgents = (Integer)parm.getValue("numAgents");
		// zoneDistance = (Double)parm.getValue("zoneDistance");

		MAX_TICKS = (int) (TICKS_PER_SECOND * 3600 * HOURS);
		RunEnvironment.getInstance().endAt(MAX_TICKS);
		RunEnvironment.getInstance().setScheduleTickDelay(70);
	}

	public static Agent getAgent(Context<?> context, String name)
	{
		for (Object obj : context.getObjects(Agent.class))
		{
			if (((Agent) obj).getAID().getName().equals(name))
			{
				return (Agent) obj;
			}
		}
		return null;
	}

	public static Agent getAgentByID(Context<?> context, String name)
	{
		for (Object obj : context.getObjects(Agent.class))
		{
			if (obj instanceof RoadAgent && ((RoadAgent) obj).getID() == Integer.parseInt(name))
				;
			return (Agent) obj;
		}
		return null;
	}

	public RoadAgent getAgent(int i)
	{
		String id = RoadAgent.makeFullName(i);

		for (RoadAgent agent : this.agents)
		{
			if (agent.getAID().getName().equals(id))
				return agent;
		}
		return null;
	}

	public Point semaphoreLocation(int numAgent)
	{
		int x, y;

		switch (numAgent)
		{
			case 1:
				x = 3;
				y = 15;
				break;
			case 2:
				x = 5;
				y = 17;
				break;
			case 3:
				x = 3;
				y = 5;
				break;
			case 4:
				x = 5;
				y = 13;
				break;
			case 5:
				x = 5;
				y = 1;
				break;
			case 6:
				x = 5;
				y = 3;
				break;
			case 7:
				x = 1;
				y = 15;
				break;
			case 8:
				x = 3;
				y = 13;
				break;
			case 9:
				x = 1;
				y = 3;
				break;
			case 10:
				x = 3;
				y = 3;
				break;
			case 11:
				x = 5;
				y = 15;
				break;
			case 12:
				x = 10;
				y = 10;
				break;
			case 13:
				x = 5;
				y = 5;
				break;
			case 14:
				x = 10;
				y = 8;
				break;
			case 15:
				x = 12;
				y = 10;
				break;
			case 16:
				x = 13;
				y = 12;
				break;
			case 17:
				x = 12;
				y = 8;
				break;
			case 18:
				x = 13;
				y = 6;
				break;
			default:
				return null;
		}

		return new Point(x, y);
	}

	private void putOnGrid(int i, Agent agent)
	{
		context.add(agent);
		Point location = semaphoreLocation(i);
		if (location != null)
		{
			this.grid.moveTo(agent, (int) location.getX(), (int) location.getY());
		}
	}
}