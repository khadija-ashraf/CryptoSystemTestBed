package org.secure.payment.model;

public class BroadcastedPacket {
	
	Integer nodeId;
	long packetId;
	
	public BroadcastedPacket(Integer nodeId, long packetId) {
		this.nodeId = nodeId;
		this.packetId = packetId;
	}
	
	public Integer getNodeId() {
		return nodeId;
	}
	
	public void setNodeId(Integer nodeId) {
		this.nodeId = nodeId;
	}
	
	public long getPacketId() {
		return packetId;
	}
	
	public void setPacketId(long packetId) {
		this.packetId = packetId;
	}
}
