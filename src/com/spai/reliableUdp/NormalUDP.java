package com.spai.reliableUdp;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import net.rudp.ReliableSocket;

public class NormalUDP {
	
	 public static void main(String args[]) throws Exception
	   {
	     
	      DatagramSocket clientSocket = new DatagramSocket();
	    
	      InetAddress IPAddress = InetAddress.getByName("116.202.109.82");
	      byte[] sendData = new byte[1024];
	      String sentence = "Simple udp";
	      
	      for(int i=0;i<=1000;i++)
	      {
	    	  String s=sentence+" "+i;
	    	  sendData = s.getBytes();
	      
	    	  DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, 6700);
	    	  clientSocket.send(sendPacket);
	      }
	      clientSocket.close();
	   }	
	

}
