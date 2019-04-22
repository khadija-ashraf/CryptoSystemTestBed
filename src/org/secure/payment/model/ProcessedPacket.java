package org.secure.payment.model;

public class ProcessedPacket {
	Integer nodeId;
	long uniqueId;
	PacketType packetType;
	
	public ProcessedPacket (Integer nodeId, long uniqueId) {
		this.nodeId = nodeId;
		this.uniqueId = uniqueId;
	}
	
	public ProcessedPacket (Integer nodeId, long uniqueId, PacketType packetType) {
		this.nodeId = nodeId;
		this.uniqueId = uniqueId;
		this.packetType = packetType;
	}
	
	public Integer getNodeId() {
		return nodeId;
	}
	
	public void setNodeId(Integer nodeId) {
		this.nodeId = nodeId;
	}

	public long getUniqueId() {
		return uniqueId;
	}

	public void setUniqueId(long uniqueId) {
		this.uniqueId = uniqueId;
	}

	public PacketType getPacketType() {
		return packetType;
	}

	public void setPacketType(PacketType packetType) {
		this.packetType = packetType;
	}
}
