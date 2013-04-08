package com.spai.reliableUdp;

import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.Charset;

import net.rudp.ReliableSocket;

public class RudpServer {

	
	private static String localAddr = "192.168.1.3";
	private static int port = 6700;
	private static int localPort = 6051;
	private static String addr = "116.202.109.82";

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		try {

			String str = "Reliable udp";
			InetAddress address = InetAddress.getByName(addr);
			InetAddress local = InetAddress.getByName(localAddr);
			
			DatagramSocket scok=new DatagramSocket();
			ReliableSocket socket=new ReliableSocket(scok);
			
			
			
			

			

			for (int i = 0; i <= 10000; i++) {
				String s = str + " " + i + "\n";
				

				DatagramPacket p=new DatagramPacket(s.getBytes(),s.getBytes().length,re,remotePort);
				scok.send(p);
			
				out.write(s.getBytes(Charset.forName("UTF-8")));
			}
			out.close();
			
			while(true);
			
			//socket.close();
		} catch (Exception e) {
			// TODO: handle exception

			e.printStackTrace();
		}

	}

}
