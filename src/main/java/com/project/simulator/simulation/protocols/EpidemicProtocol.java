package com.project.simulator.simulation.protocols;

import com.project.simulator.entity.Message;
import com.project.simulator.entity.Node;

public class EpidemicProtocol extends MessageTransmissionProtocol {

	@Override
	protected boolean shouldTransfer(Node fromNode, Node toNode, Message message) {
		return true;
	}
	
}
