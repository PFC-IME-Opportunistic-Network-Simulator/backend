package com.project.simulator.generator.messageGenerator;

import java.util.ArrayList;
import java.util.List;

import com.project.simulator.entity.Pair;

public class MessageGeneratorConfiguration {
	private Long sourceNodeId;
	private Long destinationNodeId;
	
	private MessageGeneratorConfiguration(Long sourceNodeId, Long destinationNodeId) {
		this.sourceNodeId = sourceNodeId;
		this.destinationNodeId = destinationNodeId;
	}
	
	public static MessageGeneratorConfiguration randomNodes() {
		return new MessageGeneratorConfiguration(null, null);
	}
	
	public static List<MessageGeneratorConfiguration> fixedNodes(long sourceNodeId, long destinationNodeId) {
		List<MessageGeneratorConfiguration> configList = new ArrayList<MessageGeneratorConfiguration>();
		configList.add(new MessageGeneratorConfiguration(sourceNodeId, destinationNodeId));
		return configList;
	}

	public static List<MessageGeneratorConfiguration> allPairs(int numberOfNodes){
		List<MessageGeneratorConfiguration> configList = new ArrayList<MessageGeneratorConfiguration>();
		for(int i = 0; i < numberOfNodes; i++) {
			for(int j = 0; j < numberOfNodes; j++) {
				if(i!=j) {
				configList.add(new MessageGeneratorConfiguration((long) i, (long) j));
				}
			}
		}
		return configList;
	}
	
	public Long getSourceNodeId() {
		return sourceNodeId;
	}

	public Long getDestinationNodeId() {
		return destinationNodeId;
	}
}
