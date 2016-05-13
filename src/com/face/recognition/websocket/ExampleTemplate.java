package com.face.recognition.websocket;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.imageio.ImageIO;
import javax.servlet.ServletContext;

import org.ddogleg.struct.FastQueue;
import org.ddogleg.struct.GrowQueue_I32;

import boofcv.abst.distort.FDistort;
import boofcv.abst.flow.DenseOpticalFlow;
import boofcv.abst.segmentation.ImageSuperpixels;
import boofcv.alg.color.ColorHsv;
import boofcv.alg.feature.detect.template.TemplateMatching;
import boofcv.alg.feature.detect.template.TemplateMatchingIntensity;
import boofcv.alg.filter.blur.GBlurImageOps;
import boofcv.alg.misc.ImageStatistics;
import boofcv.alg.misc.PixelMath;
import boofcv.alg.segmentation.ComputeRegionMeanColor;
import boofcv.alg.segmentation.ImageSegmentationOps;
import boofcv.factory.feature.detect.template.FactoryTemplateMatching;
import boofcv.factory.feature.detect.template.TemplateScoreType;
import boofcv.factory.flow.FactoryDenseOpticalFlow;
import boofcv.factory.segmentation.ConfigFh04;
import boofcv.factory.segmentation.FactoryImageSegmentation;
import boofcv.factory.segmentation.FactorySegmentationAlg;
import boofcv.gui.PanelGridPanel;
import boofcv.gui.feature.VisualizeOpticalFlow;
import boofcv.gui.feature.VisualizeRegions;
import boofcv.gui.image.AnimatePanel;
import boofcv.gui.image.ShowImages;
import boofcv.gui.image.VisualizeImageData;
import boofcv.io.UtilIO;
import boofcv.io.image.ConvertBufferedImage;
import boofcv.io.image.UtilImageIO;
import boofcv.struct.feature.ColorQueue_F32;
import boofcv.struct.feature.Match;
import boofcv.struct.flow.ImageFlow;
import boofcv.struct.image.GrayF32;
import boofcv.struct.image.GrayS32;
import boofcv.struct.image.GrayU8;
import boofcv.struct.image.ImageBase;
import boofcv.struct.image.ImageType;
import boofcv.struct.image.Planar;
import georegression.metric.UtilAngle;

public class ExampleTemplate {
	
	private GrayF32 templateCursor;
	private GrayF32 maskCursor;
	
	ExampleTemplate(ServletContext servletContext){
		
		String filename1 = servletContext.getRealPath("/WEB-INF/resources/teste.png");
		String filename2 = servletContext.getRealPath("/WEB-INF/resources/teste_mask.png");
		
		File f = new File(filename1);
         
	    // returns true if the file exists
	    boolean bool = f.exists();
		
		templateCursor = UtilImageIO.loadImage(UtilIO.pathExample(filename1), GrayF32.class);
		maskCursor = UtilImageIO.loadImage(UtilIO.pathExample(filename2), GrayF32.class);
		
	};
	
	public byte[] convert(byte[] imageData) throws IOException{
		
		InputStream in = new ByteArrayInputStream(imageData);
		BufferedImage image = ImageIO.read(in);
		
		// create output image to show results
		in = new ByteArrayInputStream(imageData);
		BufferedImage output = ImageIO.read(in);
		Graphics2D g2 = output.createGraphics();
		
		ImageType<GrayF32> imageType = ImageType.single(GrayF32.class);
		ImageBase frame = imageType.createImage(image.getWidth(),image.getHeight());
		GrayF32 finalImage = (GrayF32) frame;
		
		// Search for the cursor in the image.  For demonstration purposes it has been pasted 3 times
		g2.setColor(Color.RED); g2.setStroke(new BasicStroke(5));
		drawRectangles(g2, finalImage, templateCursor, maskCursor, 3);
		// show match intensity image for this template
		showMatchIntensity(finalImage, templateCursor, maskCursor);
		
		// Now it's try finding the cursor without a mask.  it will get confused when the background is black
		g2.setColor(Color.BLUE); g2.setStroke(new BasicStroke(2));
		drawRectangles(g2, finalImage, templateCursor, null, 3);
 
		// Now it searches for a specific icon for which there is only one match
		//g2.setColor(Color.ORANGE); g2.setStroke(new BasicStroke(3));
		//drawRectangles(g2, finalImage, templatePaint, null, 1);
 
		//ShowImages.showWindow(output, "Found Matches",true);

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ImageIO.write(output, "jpg", baos);
		byte[] bytes = baos.toByteArray();
		return bytes;

	}
	
	private static void drawRectangles(Graphics2D g2, GrayF32 image, GrayF32 template, GrayF32 mask, int expectedMatches) {
		   
			   
			List<Match> found = findMatches(image, template, mask, expectedMatches);
			
			int r = 2;
			int w = template.width + 2 * r;
			int h = template.height + 2 * r;
			
			for (Match m : found) {
			// the return point is the template's top left corner
			int x0 = m.x - r;
			int y0 = m.y - r;
			int x1 = x0 + w;
			int y1 = y0 + h;
			
			g2.drawLine(x0, y0, x1, y0);
			g2.drawLine(x1, y0, x1, y1);
			g2.drawLine(x1, y1, x0, y1);
			g2.drawLine(x0, y1, x0, y0);
		}
	}
	
	private static List<Match> findMatches(GrayF32 image, GrayF32 template, GrayF32 mask,
			   int expectedMatches) {
		// create template matcher.
		TemplateMatching<GrayF32> matcher =
		FactoryTemplateMatching.createMatcher(TemplateScoreType.SUM_DIFF_SQ, GrayF32.class);
		
		// Find the points which match the template the best
		matcher.setTemplate(template, mask,expectedMatches);
		matcher.process(image);
		
		return matcher.getResults().toList();
	
	}
	
	public static void showMatchIntensity(GrayF32 image, GrayF32 template, GrayF32 mask) {
		 
		// create algorithm for computing intensity image
		TemplateMatchingIntensity<GrayF32> matchIntensity =
				FactoryTemplateMatching.createIntensity(TemplateScoreType.SUM_DIFF_SQ, GrayF32.class);
 
		// apply the template to the image
		matchIntensity.process(image, template, mask);
 
		// get the results
		GrayF32 intensity = matchIntensity.getIntensity();
 
		// adjust the intensity image so that white indicates a good match and black a poor match
		// the scale is kept linear to highlight how ambiguous the solution is
		float min = ImageStatistics.min(intensity);
		float max = ImageStatistics.max(intensity);
		float range = max - min;
		PixelMath.plus(intensity, -min, intensity);
		PixelMath.divide(intensity, range, intensity);
		PixelMath.multiply(intensity, 255.0f, intensity);
 
		BufferedImage output = new BufferedImage(image.width, image.height, BufferedImage.TYPE_INT_BGR);
		VisualizeImageData.grayMagnitude(intensity, output, -1);
		
		/*ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ImageIO.write(output, "jpg", baos);
		byte[] bytes = baos.toByteArray();
		return bytes;*/
		
	}
	
}
