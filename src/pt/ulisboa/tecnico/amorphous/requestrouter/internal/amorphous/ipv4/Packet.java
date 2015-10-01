package pt.ulisboa.tecnico.amorphous.requestrouter.internal.amorphous.ipv4;

import java.net.InetAddress;

class Packet{
	private InetAddress dst = null;
	private byte[] payload = null;
	
	public Packet(InetAddress dst, byte[] payload){
		this.dst = dst;
		this.payload = payload;
	}
	
	public InetAddress getDestination(){
		return this.dst;
	}
	
	public byte[] getPayload(){
		return this.payload;
	}
}