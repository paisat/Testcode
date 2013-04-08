package com.spai.ImageTest;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import javax.imageio.ImageIO;

import net.rudp.ReliableSocket;

public class Screentest2 {
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		try {
			String local = "10.0.0.3";
			String remote = "10.0.0.4";
			int localPort = 6678;
			int remotePort = 6610;
			InetAddress lo = InetAddress.getByName(local);
			InetAddress re = InetAddress.getByName(remote);

			DatagramSocket scok=new DatagramSocket();
			ReliableSocket socket=new ReliableSocket(scok);

			File img1 = new File("/home/spai/screenshot/0.jpeg");
			BufferedImage im1 = ImageIO.read(img1);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ImageIO.write(im1, "jpeg", baos);
			
			DatagramPacket p=new DatagramPacket(baos.toByteArray(),baos.toByteArray().length,re,remotePort);
			scok.send(p);
		
			
		
			
			
			socket.close();

			

		} catch (Exception e) {
			// TODO: handle exception

			e.printStackTrace();
		}

	}
	

}
