package com.project.interfaces.commandLine.report;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;


import com.project.interfaces.commandLine.parser.FileNamesParser;
import com.project.simulator.entity.SimulationReport;

public class Reporter {
	
	public static void report(SimulationReport report) throws IOException {
		
		String reportMessage = "Delivery ratio: " + report.getDeliveryRatio()
								+ "\n" + "Average delay: " + report.getAverageDelay();
		File outputFile = FileNamesParser.outputReportFile();
	    BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));
	    writer.write(reportMessage);
	    writer.close();
	    System.out.println("Simulation Success!");
	    System.out.println("Report output wrote in file " + outputFile);
	}

}
