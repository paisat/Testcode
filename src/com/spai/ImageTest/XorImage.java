package com.spai.ImageTest;

import java.awt.Color;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.image.BufferedImage;
import java.awt.image.IndexColorModel;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.util.zip.GZIPOutputStream;

import javax.media.jai.RenderedOp;
import javax.media.jai.operator.ColorQuantizerDescriptor;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import com.gif4j.quantizer.Quantizer;

import net.rudp.ReliableSocket;

public class XorImage {

	private static int width = 200;
	private static int height = 150;
	private static int[] value = new int[3];
	private static final int CONSTANT_THRESH = 90;
	private static final int RANDOM_THRESH = 91;
	private static final int ORDERED_DITHER = 92;
	private static final int ERROR_DIFFUSION = 93;
	static double threshold = 0.0;
	static int numLevels = 8;

	static int ditherType = CONSTANT_THRESH;

	private static BufferedImage image;

	public static void quantizePixel(int col, int row) {

		int rgb = image.getRGB(col, row);
		Color c = new Color(rgb);
		value[0] = c.getRed();
		value[1] = c.getGreen();
		value[2] = c.getBlue();

		// Determine the "eta" threshold as determined by dither type
		double eta = threshold;

		for (int n = 0; n < 3; ++n) {

			// Rescale to range 0-1
			double f = value[n] / 255.0;

			// Determine level (will be 0,..,K-1)
			double fquant = Math.floor((numLevels - 1) * f + eta);

			// Quantization error
			double ferror = fquant / (numLevels - 1) - f;

			// Put in range 0-255 so that it can be displayed
			value[n] = (int) Math.floor((fquant / (numLevels - 1)) * 255);

		}

		// System.out.println(value[0]+" "+value[1]+" "+value[1]);

		// Make rgb and assign to appropriate image
		Color c2 = new Color(value[0], value[1], value[2]);

		image.setRGB(col, row, c2.getRGB());

	}

	public static BufferedImage getImageFromArray(int[] pixels, int width,
			int height) {
		BufferedImage image = new BufferedImage(width, height,
				BufferedImage.TYPE_3BYTE_BGR);
		image.setRGB(0, 0, width, height, pixels, 0, width);
		return image;
	}

	public static void reQuantize() {

		// System.out.println("Dither type=" + ditherType + " threshold="
		// + threshold + " levels=" + numLevels);

		// Loop over every pixel
		for (int col = 0; col < width; ++col) {
			for (int row = 0; row < height; ++row) {

				quantizePixel(col, row);
			}

		}
	}

	public static void main(String args[])

	{

		JLabel labelImage = new JLabel();

		JFrame frame = new JFrame("Multicast Image Receiver");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().add(labelImage);
		frame.setSize(480, 360);
		frame.setVisible(true);
		frame.setAlwaysOnTop(true);

		boolean first = true;

		int[] oldFrame = new int[width * height];
		BufferedImage imageB = null;

		try {

			String localaddr = "10.0.0.3";
			String remote = "10.0.0.4";
			int port = 6700;
			int local = 6500;

			InetAddress lo = InetAddress.getByName(localaddr);
			InetAddress re = InetAddress.getByName(remote);

			Robot robot = new Robot();
			Rectangle rectangle = new Rectangle(0, 0, width, height);

		//	ReliableSocket socket = new ReliableSocket(re, port, lo, local);
		//	OutputStream outStream = socket.getOutputStream();

			while (true) {

				boolean hasCghanges = false;
				int[] newFrame = new int[width * height];
				image = robot.createScreenCapture(rectangle);

				image = Quantizer.quantize(Quantizer.MEMORY_LOW_FAST,image, 3);
				image.getRGB(0, 0, width, height, newFrame, 0, width);

				if (first) {

					/*System.out.println("inside else");
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					DataOutputStream dos = new DataOutputStream(baos);
					for (int i = 0; i < newFrame.length; ++i) {
						dos.writeInt(newFrame[i]);

					}

					byte[] firstFrame = baos.toByteArray();
					ByteArrayOutputStream out = new ByteArrayOutputStream();
					GZIPOutputStream ou = new GZIPOutputStream(out);
					ou.write(firstFrame);
					ou.close();
					System.out.println("Size " + out.toByteArray().length);
					out.flush();

					outStream.write(out.toByteArray());
					out.close();

					outStream.flush();*/

				} else {

					int length = newFrame.length;
					int xorFrame[] = new int[length];

					for (int i = 0; i < length; i++) {
						xorFrame[i] = (oldFrame[i] ^ newFrame[i]);

						if (xorFrame[i] != 0) {
							// System.out.println(xorFrame[i]);
							// System.out.println("not same");
							hasCghanges = true;
						}

					}

					if (hasCghanges) {
						ByteArrayOutputStream baos = new ByteArrayOutputStream();
						DataOutputStream dos = new DataOutputStream(baos);
						for (int i = 0; i < xorFrame.length; ++i) {
							dos.writeInt(xorFrame[i]);
						}

						byte[] xor = baos.toByteArray();

						ByteArrayOutputStream out = new ByteArrayOutputStream();
						GZIPOutputStream ou = new GZIPOutputStream(out);
						ou.write(xor);
						ou.close();
						System.out.println("size " +out.size());
						out.flush();
						out.close();
					} else {
						//System.out.println("No changes");
					}

					
					  int decompress[] = new int[width * height];
					  
					  for (int i = 0; i < length; i++) { decompress[i] =
					  xorFrame[i] ^ oldFrame[i];
					  
					  }
					  
					  BufferedImage image = getImageFromArray(decompress,
					  width, height); labelImage.setIcon(new ImageIcon(image));
					  frame.pack();
					 

				}

				oldFrame = newFrame;
				// imageB=image;

				first = false;

			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
