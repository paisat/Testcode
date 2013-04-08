package com.spai.ImageTest;

import java.io.File;

public class colordepth {
	
	public static void main(String args[])
	{
		// Read input image
	    val image = loadMatAndShowOrExit(new File("data/boldt.jpg"), CV_LOAD_IMAGE_COLOR)

	    // Add salt noise
	    val dest = colorReduce(image)

	    // Display
	    show(dest, "Reduced colors")


	    /**
	     * Reduce number of colors.
	     * @param image input image.
	     * @param div color reduction factor.
	     */
	    def colorReduce(image: CvMat, div: Int = 64): CvMat = {

	        // Total number of elements, combining components from each channel
	        val nbElements = image.rows * image.cols * image.channels
	        for (i <- 0 until nbElements) {
	            // Convert to integer
	            val v = image.get(i).toInt
	            // Use integer division to reduce number of values
	            val newV = v / div * div + div / 2
	            // Put back into the image
	            image.put(i, newV)
	        }

	        image
	    }
	}

}
