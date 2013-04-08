package com.spai.ImageTest;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBufferByte;
import java.awt.image.IndexColorModel;
import java.awt.image.WritableRaster;
import java.io.ByteArrayOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Iterator;
import java.util.zip.GZIPOutputStream;

import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;

import com.gif4j.quantizer.Quantizer;

public class SendImage {

	private static InetAddress ia;
	private static int port = 6600;
	private static DatagramSocket socket;
	private static int header_size = 8;
	private static int max_packets = 255;
	private static int session_start = 1;
	private static int session_end = 2;
	private static int Max_Datagram_size = 1408 - header_size;
	private static int max_session_number = 255;

	private static byte[] bufferedImageToByteArray(BufferedImage image,
			String format) {
		
		
		
		
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		try {

			ImageIO.write(image, format, baos);

		} catch (Exception e) {
			// TODO: handle exception
		}

		return baos.toByteArray();
	}

	private static void send(byte[] imageByteArray) {

		int sessionNumber = 0;

		int packets = (int) Math.ceil(imageByteArray.length
				/ (float) Max_Datagram_size);

		// System.out.println("total size "+imageByteArray.length);
		// System.out.println("packets "+packets);

		for (int i = 0; i <= packets; i++) {
			int flags = 0;

			flags = (i == 0) ? session_start : flags;
			flags = (i + 1) * Max_Datagram_size > imageByteArray.length ? session_end
					: flags;
			int size = (flags != session_end) ? Max_Datagram_size
					: imageByteArray.length - i * Max_Datagram_size;

			/*
			 * System.out.println("Size "+size);
			 * System.out.println("Flags "+flags);
			 * System.out.println("max size "+(DATAGRAM_MAX_SIZE>>8) );
			 */

			byte[] data = new byte[header_size + size];
			data[0] = (byte) flags;
			data[1] = (byte) sessionNumber;
			data[2] = (byte) packets;
			data[3] = (byte) (Max_Datagram_size >> 8);
			data[4] = (byte) Max_Datagram_size;
			data[5] = (byte) i;
			data[6] = (byte) (size >> 8);
			data[7] = (byte) size;

			System.arraycopy(imageByteArray, i * Max_Datagram_size, data,
					header_size, size);

			// System.out.println("inside image send");

			try {
				DatagramPacket packet = new DatagramPacket(data, data.length,
						ia, port);

				socket.send(packet);
			} catch (Exception e) {
				// TODO: handle exception

				e.printStackTrace();
			}

			/*
			 * cansend = false;
			 * 
			 * while (!cansend) {
			 * 
			 * byte[] buffer1 = new byte[512]; DatagramPacket dp = new
			 * DatagramPacket(buffer1, buffer1.length);
			 * congestionControl.receive(dp); String s = new
			 * String(dp.getData(), 0, dp.getLength(), "UTF-8");
			 * 
			 * if (s.equals("send")) {
			 * 
			 * cansend = true; }
			 * 
			 * }
			 */

			if (flags == session_end)
				break;

		}

		sessionNumber = sessionNumber < max_session_number ? ++sessionNumber
				: 0;
		Runtime.getRuntime().gc();

	}

	public static BufferedImage convertRGBAToIndexed(BufferedImage src) {
		BufferedImage dest = new BufferedImage(src.getWidth(), src.getHeight(),
				BufferedImage.TYPE_BYTE_INDEXED);
		Graphics g = dest.getGraphics();
		g.setColor(new Color(231, 20, 189));

		// fill with a hideous color and make it transparent
		g.fillRect(0, 0, dest.getWidth(), dest.getHeight());
		// dest = makeTransparent(dest, 0, 0);

		dest.createGraphics().drawImage(src, 0, 0, null);
		return dest;
	}

	public static BufferedImage makeTransparent(BufferedImage image, int x,
			int y) {
		ColorModel cm = image.getColorModel();
		if (!(cm instanceof IndexColorModel))
			return image; // sorry...
		IndexColorModel icm = (IndexColorModel) cm;
		WritableRaster raster = image.getRaster();
		int pixel = raster.getSample(x, y, 0); // pixel is offset in ICM's
												// palette
		int size = icm.getMapSize();
		byte[] reds = new byte[size];
		byte[] greens = new byte[size];
		byte[] blues = new byte[size];
		icm.getReds(reds);
		icm.getGreens(greens);
		icm.getBlues(blues);
		IndexColorModel icm2 = new IndexColorModel(8, size, reds, greens,
				blues, pixel);
		return new BufferedImage(icm2, raster, image.isAlphaPremultiplied(),
				null);
	}

	public static BufferedImage scale(BufferedImage source, int w, int h) {
		Image image = source
				.getScaledInstance(w, h, Image.SCALE_AREA_AVERAGING);
		BufferedImage result = new BufferedImage(w, h,
				BufferedImage.TYPE_INT_RGB);
		Graphics2D g = result.createGraphics();
		g.drawImage(image, 0, 0, null);
		g.dispose();
		return result;
	}

	/**
	 * Shrinks a BufferedImage
	 * 
	 * @param source
	 *            Image to shrink
	 * @param factor
	 *            Scaling factor
	 * @return Scaled image
	 */
	public static BufferedImage shrink(BufferedImage source, double factor) {
		int w = (int) (source.getWidth() * factor);
		int h = (int) (source.getHeight() * factor);
		return scale(source, w, h);
	}

	public static void justSend(byte[] data) {
		try {
			DatagramPacket packet = new DatagramPacket(data, data.length, ia,
					port);

			socket.send(packet);
		} catch (Exception e) {
			// TODO: handle exception

			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		try {

			String address = "180.215.70.68";
			int width = 200;
			int height = 150;

			ia = InetAddress.getByName(address);

			socket = new DatagramSocket();
			boolean first = true;
			int prevFrame[] = new int[width * height];

			Robot robot = new Robot();
			Rectangle rectangle = new Rectangle(0, 0, width, height);

			while (true) {

				int currentFrame[] = new int[width * height];
				BufferedImage image = robot.createScreenCapture(rectangle);

		

				// image=ConvertUtil.convert8(image);
				image.getRGB(0, 0, width, height, currentFrame, 0, width);
				image=Quantizer.quantize(Quantizer.MEMORY_LOW_FAST_DITHER,image, 3);
				byte img[] = bufferedImageToByteArray(image, "jpeg");
				// byte img[] = ((DataBufferByte)image.getRaster().getDataBuffer()).getData();

				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				GZIPOutputStream gzipOut = new GZIPOutputStream(baos);
				gzipOut.write(img);
				gzipOut.close();

				byte cmpImage[] = baos.toByteArray();
				baos.flush();
				baos.close();

				if (first) {

					justSend(cmpImage);
					first = false;
					prevFrame = currentFrame;

				} else {
					int inCursor = 0;
					int size = currentFrame.length;
					boolean same = true;

					while (inCursor < size) {
						if (prevFrame[inCursor] != currentFrame[inCursor]) {
							same = false;
						}
						inCursor++;
					}

					if (!same) {
						justSend(cmpImage);
						prevFrame = currentFrame;
						System.out.println("not same " + img.length);
					} else {
						prevFrame = currentFrame;
					}

				}
			}

		} catch (Exception e) {
			// TODO: handle exception

			e.printStackTrace();
		}
	}

}
