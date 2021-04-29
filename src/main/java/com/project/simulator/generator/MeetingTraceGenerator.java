package com.project.simulator.generator;

import com.project.simulator.entity.Meet;
import com.project.simulator.entity.MeetingTrace;
import com.project.simulator.entity.MeetingTraceGeneratorInput;
import com.project.simulator.entity.Pair;

public class MeetingTraceGenerator {
	private static void generatePairMeetings(MeetingTrace meetingTrace, Pair pair, double totalSimulationTime) {
		double lastMeetInstant = 0;

		while(lastMeetInstant < totalSimulationTime) {
			lastMeetInstant = lastMeetInstant + pair.generateDt(lastMeetInstant == 0);
			meetingTrace.addMeet(new Meet(pair, lastMeetInstant));
		}
	}
	
	public static MeetingTrace generateMeetingTrace(MeetingTraceGeneratorInput input) {
		MeetingTrace meetingTrace = new MeetingTrace();
		for(Pair pair : input.getPairs()) {
			if(pair.pairMeets()) MeetingTraceGenerator.generatePairMeetings(meetingTrace, pair, input.getTotalSimulationTime());
		}
		meetingTrace.orderTrace();
		return meetingTrace;
	}
}
