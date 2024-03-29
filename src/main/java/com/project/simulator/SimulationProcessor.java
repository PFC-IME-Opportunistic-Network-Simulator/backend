package com.project.simulator;

import com.project.exception.SimulatorException;
import com.project.interfaces.commandLine.report.CommandLineReporter;
import com.project.simulator.configuration.SimulationConfiguration;
import com.project.simulator.entity.SimulationReport;
import com.project.simulator.threadHandler.SimulationThreadHandler;

import lombok.Getter;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Getter
public class SimulationProcessor {
	
    private final SimulationConfiguration config;
    private final List<SimulationThreadHandler> threads;
    private SimulationReport report;
    boolean done;
    CommandLineReporter reporter;
	String prefix;
    
    public SimulationProcessor(SimulationConfiguration config) {
        this.config = config;
        this.threads = new ArrayList<>();
        this.reporter = CommandLineReporter.makeRoot();
        this.done = false;
		this.prefix = null;
    }

	public SimulationProcessor(SimulationConfiguration config, String prefix) {
		this.config = config;
		this.threads = new ArrayList<>();
		this.reporter = CommandLineReporter.make(prefix);
		this.done = false;
		this.prefix = prefix;
	}

    public SimulationReport runSimulation() {
    	
    	for(int i = 0; i < this.config.getNumberOfRounds(); i++) {
    		
    		while(!canInitiateNewThread()) {
    			
    			wasAnyThreadInterrupted();
    			
    			try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
    		}
    		try {
    			
    			SimulationThreadHandler newThread = new SimulationThreadHandler(config, CommandLineReporter.make(getSimulationPrefix(String.valueOf(i + 1))));
    			threads.add(newThread);
    			newThread.start();
    			System.out.println("begin thread " + i);
			} catch(Exception e) { }
    	}
    	
    	while(true) {
    		
    		wasAnyThreadInterrupted();
    		
    		if(this.checkAllThreadsDone()) {
    			this.report = this.calculateSimulationReportAverage();
    			reporter.reportSimulationSummary(this.report);
    			this.done = true;
				return this.report;
    		}
    		
    		try {
				Thread.sleep(100); //intervalo de tempo entre as verificações de término das threads
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
    	}
        
    }

	private String getSimulationPrefix(String suffix) {
		if (this.prefix != null) return this.prefix + File.separator + suffix;
		else return suffix;
	}
    
    private boolean canInitiateNewThread() {
		return this.runningThreads() <= 5;
	}
    
    private void wasAnyThreadInterrupted() {
    	this.getProgress();
    	for (SimulationThreadHandler thread : this.threads) {
    		if(thread.isError())
    			throw new SimulatorException(thread.getErrorMessage());
    	}
    	
    }

    private boolean checkAllThreadsDone() {
    	for(SimulationThreadHandler thread : threads) {
    		if (thread.isRunning()) return false;
		}
    	return true;
	}

	public int runningThreads() {
		int count = 0;
    	for(SimulationThreadHandler thread : threads) {
			if (thread.isRunning()) count++;
		}
		return count;
	}
	
	public int finishedThreads() {
		int count = 0;
    	for(SimulationThreadHandler thread : threads) {
			if (thread.isDone()) count++;
		}
		return count;
	}

	private SimulationReport calculateSimulationReportAverage() {
		List<Double> delayList = new ArrayList<>();
		List<Double> deliveryRatioList = new ArrayList<>();

		List<SimulationReport> reports = this.getAllSimulationReports();

		for(SimulationReport simulationPartialReport : reports) {
			delayList.add(simulationPartialReport.getAverageDelay());
			deliveryRatioList.add(simulationPartialReport.getDeliveryRatio());
		}

		double finalAverageDelay = delayList.stream().mapToDouble(Double::doubleValue).average().getAsDouble();
		double finalDeliveryRatio = deliveryRatioList.stream().mapToDouble(Double::doubleValue).average().getAsDouble();

		double variance = 0;
		for (Double delay : delayList) {
			variance += Math.pow(delay - finalAverageDelay, 2);
		}
		variance /= delayList.size();

		double std = Math.sqrt(variance);

		return new SimulationReport(finalAverageDelay, finalDeliveryRatio, variance, std);
	}

	private List<SimulationReport> getAllSimulationReports() {
    	List<SimulationReport> simulationReports = new ArrayList<>();

    	for (SimulationThreadHandler thread : threads) {
    		simulationReports.add(thread.getReport());
		}

    	return simulationReports;
	}

	public List<File> getAllSimulationFiles() {
		return this.reporter.getFileNameManager().getAllReportFiles();
	}
	
	public double getProgress() {
		double progress = 0;
		for(SimulationThreadHandler thread : this.getThreads()) {
			progress+=thread.getProgress();
		}
		progress *= 100/this.getConfig().getNumberOfRounds();
		progress = BigDecimal.valueOf(progress)
			    .setScale(2, RoundingMode.HALF_UP)
			    .doubleValue();
		System.out.println("progress: " + progress + "%");
		return progress;
	}
}
