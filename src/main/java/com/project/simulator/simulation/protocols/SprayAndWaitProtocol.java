package com.project.simulator.simulation.protocols;

import com.project.simulator.entity.Message;
import com.project.simulator.entity.Node;
import com.project.simulator.entity.NodeGroup;
import com.project.simulator.entity.event.MeetEvent;

public class SprayAndWaitProtocol extends MessageTransmissionProtocol {
	private int lValue;
	
	public SprayAndWaitProtocol(int lValue) {
		this.lValue = lValue;
	}

	@Override
	protected boolean shouldTransfer(Node fromNode, Node toNode, Message message) {
		initialStoreMessage(fromNode, message);

		if(alreadyDelivered(toNode, message))
			return false;

		int messageLValueInNodeFrom = Integer.valueOf(message.getStoredValue(String.valueOf(fromNode.getId())));

		if(messageLValueInNodeFrom == 1) {
			return wait(fromNode, toNode, message, messageLValueInNodeFrom);
		} else {
			return spray(fromNode, toNode, message, messageLValueInNodeFrom);
		}
	}

	private boolean spray(Node fromNode, Node toNode, Message message, int messageLValueInNodeFrom) {
		return true;
	}

	private boolean wait(Node fromNode, Node toNode, Message message, int messageLValueInNodeFrom) {
		if (message.getDestinationNode() == toNode.getId() || message.getSourceNode() == fromNode.getId()) {
			return true;
		}
		return false;
	}

	private boolean alreadyDelivered(Node toNode, Message message) {
		return message.hasStoredElement(String.valueOf(toNode.toString()));
	}

	// TODO: trocar isso para ser um metodo chamado na criação da mensagem
	private void initialStoreMessage(Node fromNode, Message message) {
		if(!message.hasStoredElement(String.valueOf(fromNode.getId()))) {
			message.storeValue(String.valueOf(fromNode.getId()), String.valueOf(this.lValue));
		}
	}
	
	@Override
	protected void postTransfer(Message message, Node fromNode, Node toNode) {
		handleFromNode(message, fromNode);
		handleToNode(message, toNode);
	}

	private void handleToNode(Message message, Node toNode) {
		message.storeValue(String.valueOf(toNode.getId()), String.valueOf(1));
	}

	private void handleFromNode(Message message, Node fromNode) {
		int messageLValueInNodeFrom = Integer.valueOf(message.getStoredValue(String.valueOf(fromNode.getId())));
		messageLValueInNodeFrom--;
		message.storeValue(String.valueOf(fromNode.getId()), String.valueOf(messageLValueInNodeFrom));
		if(messageLValueInNodeFrom == 0) {
			fromNode.removeMessage(message.getId());
		}
	}


}
