package com.project.simulator.threadHandler;


import java.util.List;

import com.project.interfaces.commandLine.report.CommandLineReporter;
import com.project.simulator.configuration.SimulationConfiguration;
import com.project.simulator.entity.MeetingTrace;
import com.project.simulator.entity.SimulationReport;
import com.project.simulator.entity.event.EventQueue;
import com.project.simulator.entity.event.MessageGenerationEvent;
import com.project.simulator.generator.MeetingTraceGenerator;
import com.project.simulator.generator.messageGenerator.MessageGenerator;
import com.project.simulator.generator.messageGenerator.MessageTransmissionProtocolFactory;
import com.project.simulator.simulation.Simulation;
import com.project.simulator.simulation.protocols.MessageTransmissionProtocol;

import lombok.Getter;

@Getter
public class SimulationThreadHandler extends Thread {
	
	private SimulationConfiguration config;
	private SimulationThreadReportHandler simulationThreadReportHandler;
	private boolean error;
	private String errorMessage;
	
	public SimulationThreadHandler(SimulationThreadReportHandler simulationThreadReportHandler, SimulationConfiguration config) {
		this.simulationThreadReportHandler = simulationThreadReportHandler;
		this.config = config;
		this.error = false;
	}
	
	public void run() {
		try {
			List<MessageGenerationEvent> messageGenerationQueue = MessageGenerator.generate(config.getMessageGenerationConfiguration());
	        MeetingTrace meetingTrace = MeetingTraceGenerator.generate(config.getMeetingTraceConfiguration());
	        CommandLineReporter.getReporter().reportMeetingTrace(meetingTrace);
	        EventQueue eventQueue = EventQueue.makeEventQueue(meetingTrace, messageGenerationQueue);
	        MessageTransmissionProtocol protocol = MessageTransmissionProtocolFactory.make(config.getProtocolConfiguration());
	        Simulation simulation = new Simulation(protocol, eventQueue, true);
	        simulation.start();
	        SimulationReport report = simulation.reportSimulationResult();
			CommandLineReporter reporter = CommandLineReporter.getReporter();
			reporter.reportSingleSimulation(report);
		    this.simulationThreadReportHandler.addSimulationReport(report);  
			
		} catch(Exception e) {
			e.printStackTrace();
			this.error = true;
			this.errorMessage = e.getMessage();
		}
		  
	}

}
