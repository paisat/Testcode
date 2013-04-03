package com.spai.testcode;

import java.awt.Dimension;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.zip.GZIPOutputStream;

import javax.imageio.ImageIO;

public class ScreenCapture {

	private static int widthChunk;
	private static int heightChunk;

	public static void main(String[] args) {
		Dimension screeSize = Toolkit.getDefaultToolkit().getScreenSize();
		widthChunk = screeSize.width / 4;
		heightChunk = screeSize.height / 3;

		ScreenGrabber[] grabber = new ScreenGrabber[12];
		int grabberIndex = 0;

		for (int y = 0; y < screeSize.height; y = y + heightChunk) {
			for (int x = 0; x < screeSize.width; x = x + widthChunk) {

				System.out.println("x y " + x + " " + y);
				grabber[grabberIndex] = new ScreenGrabber(x, y, widthChunk,
						heightChunk);

				grabberIndex++;

			}
		}

		try {
			for (int i = 0; i < grabberIndex; i++) {
				grabber[i].join();
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
		
		System.out.println("Total size : "+ ScreenGrabber.totalSize);

	}

	static class ScreenGrabber extends Thread {

		private Robot screenCaptureRobot;
		private int x;
		private int y;
		private int width;
		private int height;
		private Rectangle screeRectangle;
		public static int totalSize = 0;

		private synchronized void add(int size) {
			totalSize += size;
		}

		public ScreenGrabber(int x, int y, int width, int height) {
			// TODO Auto-generated constructor stub
			try {
				this.x = x;
				this.y = y;
				this.width = width;
				this.height = height;

				screeRectangle = new Rectangle(x, y, width, height);
				screenCaptureRobot = new Robot();
				start();
				
			} catch (Exception e) {
				// TODO: handle exception
			}

		}

		private byte[] bufferedImageToByteArray(BufferedImage image,
				String format) {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();

			try {

				ImageIO.write(image, format, baos);

			} catch (Exception e) {
				// TODO: handle exception
			}

			return baos.toByteArray();

		}

		public void run() {

			try {
				BufferedImage image = screenCaptureRobot
						.createScreenCapture(screeRectangle);

				byte img[] = bufferedImageToByteArray(image, "jpeg");

				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				GZIPOutputStream gzipOut = new GZIPOutputStream(baos);
				gzipOut.write(img);

				img = baos.toByteArray();

				System.out.println("x y " + x + " " + " " + y + " size : "
						+ img.length);

				add(img.length);

				ImageIO.write(image, "jpeg", new File("/home/spai/screenshot/"
						+ x + " " + y + ".jpeg"));

			} catch (Exception e) {
				// TODO: handle exception
			}

		}
	}

}
