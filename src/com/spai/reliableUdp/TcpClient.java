package com.spai.reliableUdp;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.charset.Charset;

public class TcpClient {

	
	public static void main(String argv[]) throws Exception
	 {
	  
		try
		{
	  String modifiedSentence;
	  BufferedReader inFromUser = new BufferedReader( new InputStreamReader(System.in));
	  InetAddress addr=InetAddress.getByName("116.202.109.82");
	  Socket clientSocket = new Socket(addr, 6789);
	  clientSocket.setTcpNoDelay(true);
	 
	  
	  	String str="test";
		OutputStream out = clientSocket.getOutputStream();

		for (int i = 0; i <= 1000; i++) {
			String s = str + " " + i + "\n";
			out.write(s.getBytes(Charset.forName("UTF-8")));
		}
		out.close();
	
	  clientSocket.close();
	 }
		catch (Exception e) {
			// TODO: handle exception
			
			e.printStackTrace();
		}
	 }
	
}
