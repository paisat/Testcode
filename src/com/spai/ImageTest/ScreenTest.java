package com.spai.ImageTest;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.OutputStream;
import java.io.ObjectInputStream.GetField;
import java.net.InetAddress;
import java.util.zip.GZIPOutputStream;

import javax.imageio.ImageIO;

import net.rudp.ReliableSocket;

public class ScreenTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		try {

			// baout.flush();

			// out.close();

			send("/home/spai/screenshot/0.jpeg");
			
			Thread.sleep(1000);

			send("/home/spai/screenshot/2.jpeg");

		} catch (Exception e) {
			// TODO: handle exception

			e.printStackTrace();
		}

	}

	private static void send(String s) {
		try {

			String local = "10.0.0.3";
			String remote = "10.0.0.4";
			int localPort = 6679;
			int remotePort = 6614;
			InetAddress lo = InetAddress.getByName(local);
			InetAddress re = InetAddress.getByName(remote);
			ReliableSocket socket = new ReliableSocket(re, remotePort, lo,
					localPort);

			File img1 = new File(s);
			BufferedImage im1 = ImageIO.read(img1);
			ByteArrayOutputStream baout = new ByteArrayOutputStream();
			
			ImageIO.write(im1, "jpeg", baout);
			OutputStream out=socket.getOutputStream();
			out.write(baout.toByteArray());
			out.flush();
			
			
			//socket.close();
			// socket.close();

		} catch (Exception e) {
			// TODO: handle exception
		}

	}

}
