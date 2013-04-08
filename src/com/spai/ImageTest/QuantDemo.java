package com.spai.ImageTest;

import java.util.*;
import java.util.zip.GZIPOutputStream;
import java.awt.*;
import java.awt.color.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;

import javax.imageio.*;
import javax.swing.*;
import javax.swing.event.*;
import java.awt.print.*;

/*

 Demo program to how the effects of quantization and the various dither 
 strategies

 Ian Bond 4/8/2010

 */

public class QuantDemo extends JFrame implements ActionListener, ChangeListener {

	static final int FRAME_WIDTH = 1000;
	static final int SLIDER_MIN = 0;
	static final int SLIDER_MAX = 100;
	static final int SLIDER_INIT = (SLIDER_MAX - SLIDER_MIN) / 2;

	JMenuItem openItem, quitItem;

	// Level options
	JRadioButton k2, k4, k8, k16, k32, k64, k128, k256;

	// Slider to select constant threshold
	JSlider slider;

	ImagePanel imageP;

	JComboBox ditherSelection;

	public void actionPerformed(ActionEvent evt) {

		JComponent source = (JComponent) evt.getSource();

		if (source == openItem) {
			JFileChooser chooser = new JFileChooser("./");
			int retVal = chooser.showOpenDialog(this);
			if (retVal == JFileChooser.APPROVE_OPTION) {
				File myFile = chooser.getSelectedFile();
				System.out.println("Choosing file " + myFile.getName());
				imageP.getImage(myFile);
			}
		} else if (source == quitItem) {
			System.out.println("Quitting ...");
			System.exit(0);

		} else if (source == k2) {
			imageP.setLevels(2);
			imageP.reQuantize();
		} else if (source == k4) {
			imageP.setLevels(4);
			imageP.reQuantize();
		} else if (source == k8) {
			imageP.setLevels(8);
			imageP.reQuantize();
		} else if (source == k16) {
			imageP.setLevels(16);
			imageP.reQuantize();
		} else if (source == k32) {
			imageP.setLevels(32);
			imageP.reQuantize();
		} else if (source == k64) {
			imageP.setLevels(64);
			imageP.reQuantize();
		} else if (source == k128) {
			imageP.setLevels(128);
			imageP.reQuantize();
		} else if (source == k256) {
			imageP.setLevels(256);
			imageP.reQuantize();
		} else if (source == ditherSelection) {
			String selection = (String) ditherSelection.getSelectedItem();
			if (selection == "Random Threshold") {
				imageP.setRandom();
			} else if (selection == "Ordered Dithering") {
				imageP.setOrdered();
			} else if (selection == "Error Diffusion") {
				imageP.setErrorDiffusion();
			}
			imageP.reQuantize();
		}
	}

	public void stateChanged(ChangeEvent e) {

		JSlider source = (JSlider) e.getSource();
		if (source.getValueIsAdjusting()) {
			double eta = (double) slider.getValue()
					/ (SLIDER_MAX - SLIDER_MIN + 1);
			imageP.setThreshold(eta);
			imageP.reQuantize();
		}

	}

	// Convenience function to wrap up the procedures in making a JButton
	JRadioButton makeButton(String title, JPanel panel, ButtonGroup group) {
		JRadioButton button = new JRadioButton(title);
		panel.add(button);
		button.addActionListener(this);
		group.add(button);
		return button;
	}

	public QuantDemo() {
		super("Quantization Demo");
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		JMenuBar menuBar = new JMenuBar();
		this.setJMenuBar(menuBar);

		// Add a file menu with some menu items
		JMenu fileMenu = new JMenu("File");
		menuBar.add(fileMenu);

		openItem = new JMenuItem("Open");
		openItem.addActionListener(this);
		fileMenu.add(openItem);
		quitItem = new JMenuItem("Quit");
		quitItem.addActionListener(this);
		fileMenu.add(quitItem);

		JPanel titleP = new JPanel();
		titleP.setBounds(new Rectangle(0, 0, FRAME_WIDTH, 100));
		titleP.add(new JLabel("Select number of levels"), BorderLayout.CENTER);
		titleP.setOpaque(false);

		JPanel controlP = new JPanel();
		controlP.setBounds(new Rectangle(0, 30, FRAME_WIDTH, 60));
		controlP.setOpaque(false);

		// Construct the level radio buttons
		ButtonGroup bitGroup = new ButtonGroup();
		k2 = makeButton("2", controlP, bitGroup);
		k4 = makeButton("4", controlP, bitGroup);
		k8 = makeButton("8", controlP, bitGroup);
		k16 = makeButton("16", controlP, bitGroup);
		k32 = makeButton("32", controlP, bitGroup);
		k64 = makeButton("64", controlP, bitGroup);
		k128 = makeButton("128", controlP, bitGroup);
		k256 = makeButton("256", controlP, bitGroup);
		k256.setSelected(true);

		// Control panel for dithering options
		JPanel ditherP = new JPanel();
		ditherP.setBounds(new Rectangle(0, 60, FRAME_WIDTH, 90));
		ditherP.setOpaque(false);
		ditherP.add(new JLabel("Select dithering"), BorderLayout.CENTER);

		String[] optStrings = { "Random Threshold", "Ordered Dithering",
				"Error Diffusion" };
		ditherSelection = new JComboBox(optStrings);
		ditherSelection.addActionListener(this);
		ditherP.add(ditherSelection);

		// Control panel for constant threshold slider
		JPanel sliderP = new JPanel();
		sliderP.setBounds(new Rectangle(0, 90, FRAME_WIDTH, 120));
		sliderP.setOpaque(false);

		// Slider to select constabt threshold
		slider = new JSlider();
		slider.setBorder(BorderFactory.createTitledBorder("Constant Threshold"));
		slider.addChangeListener(this);
		sliderP.add(new JLabel("0.0"));
		sliderP.add(slider, BorderLayout.NORTH);
		sliderP.add(new JLabel("1.0"));

		// Panel to display images
		imageP = new ImagePanel(FRAME_WIDTH);
		imageP.setBounds(new Rectangle(0, 160, FRAME_WIDTH, 900));
		imageP.setOpaque(false);

		Container content = this.getContentPane();
		content.setLayout(null);

		content.add(titleP);
		content.add(controlP);
		content.add(ditherP);
		content.add(sliderP);
		content.add(imageP);

		this.setSize(FRAME_WIDTH, 1000);
		this.setVisible(true);

	}

	public static void main(String args[]) {

		new QuantDemo();

	}

}

class ImagePanel extends JPanel {

	// Set x and y scaling to this value
	double STRETCH = 1.0;

	BufferedImage image = null; // Original image
	BufferedImage qimage = null; // Quantized image
	BufferedImage eimage = null; // Error image

	// Useful dimensions to keep track of
	int frameWidth;
	int numCols = 0;
	int numRows = 0;

	// Number of quantization levels
	int numLevels = 256;

	double threshold = 0.5;

	// Enumerate the different methods of dithering
	private static final int CONSTANT_THRESH = 90;
	private static final int RANDOM_THRESH = 91;
	private static final int ORDERED_DITHER = 92;
	private static final int ERROR_DIFFUSION = 93;
	

	int ditherType = CONSTANT_THRESH;

	// 2D array for dither pattern
	double[][] dMatrix;

	// Random number generator for random threshold
	Random randomizer;

	// Useful storage for RGB intermediate values
	int[] value, qerr;

	// Construct with frame width
	public ImagePanel(int fw) {
		this.frameWidth = fw;
		value = new int[3];
		qerr = new int[3];

		// Initialize random number generator with some seed
		randomizer = new Random(19580427);

		// Set up a 4X4 ordered dither pattern. Just one specific
		// example for this demo
		dMatrix = new double[4][4];
		dMatrix[0][0] = 15;
		dMatrix[0][1] = -9;
		dMatrix[0][2] = 9;
		dMatrix[0][3] = -15;
		dMatrix[0][0] = -1;
		dMatrix[0][1] = 7;
		dMatrix[0][2] = -12;
		dMatrix[0][3] = 1;
		dMatrix[0][0] = 11;
		dMatrix[0][1] = -13;
		dMatrix[0][2] = 13;
		dMatrix[0][3] = -11;
		dMatrix[0][0] = -5;
		dMatrix[0][1] = 3;
		dMatrix[0][2] = -3;
		dMatrix[0][3] = 5;
		for (int y = 0; y < 4; ++y) {
			for (int x = 0; x < 4; ++x) {
				double d = dMatrix[x][y];
				dMatrix[x][y] = 0.5 + d / 32.0;
			}
		}

	}

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

	public void getImage(File file) {

		try {
			image = ImageIO.read(file);
			numCols = image.getWidth();
			numRows = image.getHeight();
			qimage = new BufferedImage(numCols, numRows, image.getType());
			eimage = new BufferedImage(numCols, numRows, image.getType());
			STRETCH = 0.5 * (double) frameWidth / (numCols + 50);
			System.out.println(numCols + " X " + numRows + " " + STRETCH);
		} catch (IOException e) {
			e.printStackTrace();
		}
		this.reQuantize();
	}

	// Functions to choose the dithering procedure
	public void setThreshold(double value) {
		ditherType = CONSTANT_THRESH;
		threshold = value;
	}

	public void setRandom() {
		ditherType = RANDOM_THRESH;
	}

	public void setOrdered() {
		ditherType = ORDERED_DITHER;
	}

	public void setErrorDiffusion() {
		ditherType = ERROR_DIFFUSION;
	}

	public void setLevels(int k) {
		numLevels = k;
	}

	public void quantizePixel(int col, int row) {

		int rgb = image.getRGB(col, row);
		Color c = new Color(rgb);
		value[0] = c.getRed();
		value[1] = c.getGreen();
		value[2] = c.getBlue();

		// Determine the "eta" threshold as determined by dither type
		double eta = 0;
		if (ditherType == CONSTANT_THRESH) {
			eta = threshold;
		} else if (ditherType == RANDOM_THRESH) {
			eta = randomizer.nextDouble();
		} else if (ditherType == ORDERED_DITHER) {
			eta = dMatrix[col % 4][row % 4];
		}

		for (int n = 0; n < 3; ++n) {

			// Rescale to range 0-1
			double f = value[n] / 255.0;

			// Determine level (will be 0,..,K-1)
			double fquant = Math.floor((numLevels - 1) * f + eta);

			// Quantization error
			double ferror = fquant / (numLevels - 1) - f;

			// Put in range 0-255 so that it can be displayed
			value[n] = (int) Math.floor((fquant / (numLevels - 1)) * 255);
			qerr[n] = (int) Math
					.floor(255.0 * 0.5 * (ferror * (numLevels - 1) + 1));
		}
		
		//System.out.println(value[0]+" "+value[1]+" "+value[1]);

		// Make rgb and assign to appropriate image
		Color c2 = new Color(value[0], value[1], value[2]);
		Color c3 = new Color(qerr[0], qerr[1], qerr[2]);
		qimage.setRGB(col, row, c2.getRGB());
		eimage.setRGB(col, row, c3.getRGB());
		
	}

	// Use current quantized and error image
	public void diffuseErrors() {

		double[][][] fpixel = new double[3][numCols][numRows];

		double scale = (numLevels - 1) / 255.0;

		double eta = 0.5;

		for (int col = 0; col < numCols; ++col) {
			for (int row = 0; row < numRows; ++row) {
				int rgb = image.getRGB(col, row);
				fpixel[0][col][row] = scale * ((rgb >> 16) & 0xff);
				fpixel[1][col][row] = scale * ((rgb >> 8) & 0xff);
				fpixel[2][col][row] = scale * ((rgb >> 0) & 0xff);
			}
		}

		for (int n = 0; n < 3; ++n) {
			for (int row = numRows - 2; row > 0; --row) {
				for (int col = 1; col < numCols - 1; ++col) {

					double oldp = fpixel[n][col][row];
					double newp = Math.floor(oldp + eta);
					fpixel[n][col][row] = newp;
					double qe = oldp - newp;
					fpixel[n][col + 1][row] += 7 * qe / 16.0;
					fpixel[n][col - 1][row + 1] += 3 * qe / 16.0;
					fpixel[n][col][row + 1] += 5 * qe / 16.0;
					fpixel[n][col + 1][row + 1] += qe / 16.0;
				}
			}
		}

		for (int col = 0; col < numCols; ++col) {
			for (int row = 0; row < numRows; ++row) {
				for (int n = 0; n < 3; ++n) {

					double f = Math.floor(fpixel[n][col][row] + 0.5);
					double e = fpixel[n][col][row] + 0.5 - f;
					value[n] = (int) Math.floor(255.0 * f / (numLevels - 1));
					qerr[n] = (int) Math.floor(255.0 * e);
				}
				
				Color c2 = new Color(value[0], value[1], value[2]);
				Color c3 = new Color(qerr[0], qerr[1], qerr[2]);
				qimage.setRGB(col, row, c2.getRGB());
				eimage.setRGB(col, row, c3.getRGB());
			}
		}

	}

	public void reQuantize() {

	//	System.out.println("Dither type=" + ditherType + " threshold="
				//+ threshold + " levels=" + numLevels);

		if (ditherType == ERROR_DIFFUSION) {
			diffuseErrors();
		} else {
			// Loop over every pixel
			for (int col = 0; col < numCols; ++col) {
				for (int row = 0; row < numRows; ++row) {

					quantizePixel(col, row);
				}

			}
		}

		try {
			
			
			
			byte[] img = bufferedImageToByteArray(qimage, "png");

			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			GZIPOutputStream gzipOut = new GZIPOutputStream(baos);
			gzipOut.write(img);
			gzipOut.close();

			byte cmpImage[] = baos.toByteArray();

			System.out.println("Size " + cmpImage.length);

		} catch (Exception e) {

		}

		this.repaint();
	}

	public void paintComponent(Graphics g) {

		if (image != null) {
			Graphics2D g2 = (Graphics2D) g;
			g2.scale(STRETCH, STRETCH);

			// Get two images positioned side-by-side and centred horizontally
			double hgap = 10 / STRETCH;
			double screenCen = 0.5 * frameWidth / STRETCH;
			int x1 = (int) Math.floor(screenCen - numCols - hgap);
			int x2 = (int) Math.floor(screenCen + hgap);

			// Draw the colour and grayscale images side by side
			g2.drawImage(qimage, x1, 0, null);
			g2.drawImage(eimage, x2, 0, null);

			g2.drawString("Quantized image", x1, numRows + 20);
			g2.drawString("Error image", x2, numRows + 20);

			try {
			//	System.out.println("Saving");
				ImageIO.write(qimage, "png", new File("qimage.png"));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

}
