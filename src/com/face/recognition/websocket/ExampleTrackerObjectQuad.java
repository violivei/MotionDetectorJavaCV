package com.face.recognition.websocket;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

import boofcv.abst.tracker.TrackerObjectQuad;
import boofcv.factory.tracker.FactoryTrackerObjectQuad;
import boofcv.gui.feature.FancyInterestPointRender;
import boofcv.gui.feature.VisualizeFeatures;
import boofcv.gui.image.ShowImages;
import boofcv.gui.tracker.TrackerObjectQuadPanel;
import boofcv.io.MediaManager;
import boofcv.io.UtilIO;
import boofcv.io.image.ConvertBufferedImage;
import boofcv.io.image.SimpleImageSequence;
import boofcv.io.wrapper.DefaultMediaManager;
import boofcv.misc.BoofMiscOps;
import boofcv.struct.image.GrayU8;
import boofcv.struct.image.ImageBase;
import boofcv.struct.image.ImageType;
import boofcv.struct.image.InterleavedU8;
import georegression.struct.point.Point2D_F64;
import georegression.struct.shapes.Quadrilateral_F64;

public class ExampleTrackerObjectQuad {
	
	private ImageBase frame;
	private TrackerObjectQuad tracker;
	long previous = 0;
	Quadrilateral_F64 location;

	ExampleTrackerObjectQuad(byte[] imageData) throws IOException{
		//MediaManager media = DefaultMediaManager.INSTANCE;
		//String fileName = UtilIO.pathExample("tracking/wildcat_robot.mjpeg");
 
		// Create the tracker.  Comment/Uncomment to change the tracker.
		tracker =
				FactoryTrackerObjectQuad.circulant(null, GrayU8.class);
//				FactoryTrackerObjectQuad.sparseFlow(null,GrayU8.class,null);
//				FactoryTrackerObjectQuad.tld(null,GrayU8.class);
//				FactoryTrackerObjectQuad.meanShiftComaniciu2003(new ConfigComaniciu2003(), ImageType.pl(3, GrayU8.class));
//				FactoryTrackerObjectQuad.meanShiftComaniciu2003(new ConfigComaniciu2003(true),ImageType.pl(3,GrayU8.class));
 
				// Mean-shift likelihood will fail in this video, but is excellent at tracking objects with
				// a single unique color.  See ExampleTrackerMeanShiftLikelihood
//				FactoryTrackerObjectQuad.meanShiftLikelihood(30,5,255, MeanShiftLikelihoodType.HISTOGRAM,ImageType.pl(3,GrayU8.class));
 
		//SimpleImageSequence video = media.openVideo(fileName, tracker.getImageType());
 
		// specify the target's initial location and initialize with the first frame
		location = new Quadrilateral_F64(100.0,100.0,100.0,200.0,200.0,100.0,200.0,200.0);
		
		InputStream in = new ByteArrayInputStream(imageData);
		BufferedImage bImageFromConvert = ImageIO.read(in);
		
		ImageType<GrayU8> imageType = ImageType.single(GrayU8.class);
		ImageBase frame = imageType.createImage(bImageFromConvert.getWidth(),bImageFromConvert.getHeight());
		ConvertBufferedImage.convertFrom(bImageFromConvert, frame, true);
		
		tracker.initialize(frame,location);
		
		// For displaying the results
		/*TrackerObjectQuadPanel gui = new TrackerObjectQuadPanel(null);
		gui.setPreferredSize(new Dimension(frame.getWidth(),frame.getHeight()));
		gui.setBackGround((BufferedImage)video.getGuiImage());
		gui.setTarget(location,true);
		ShowImages.showWindow(gui,"Tracking Results", true);*/

		
	}
	
	public byte[] getVisualized() throws IOException {
		
		 
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		BufferedImage outScaled = new BufferedImage(frame.width,frame.height,BufferedImage.TYPE_INT_RGB);
		ImageIO.write(ConvertBufferedImage.convertTo((GrayU8) frame, outScaled), "jpg", baos);
		byte[] bytes = baos.toByteArray();
		return bytes;
	}
	
	public byte[] convert(byte[] imageData) throws IOException{
		
		InputStream in = new ByteArrayInputStream(imageData);
		BufferedImage bImageFromConvert = ImageIO.read(in);
		
		ImageType<GrayU8> imageType = ImageType.single(GrayU8.class);
		ImageBase frame = imageType.createImage(bImageFromConvert.getWidth(),bImageFromConvert.getHeight());
		ConvertBufferedImage.convertFrom(bImageFromConvert, frame, true);
				 
		boolean visible = tracker.process(frame,location);
		
		/*gui.setBackGround((BufferedImage) video.getGuiImage());
		gui.setTarget(location, visible);
		gui.repaint();*/
		
		// shoot for a specific frame rate
		/*long time = System.currentTimeMillis();
		BoofMiscOps.pause(Math.max(0,80-(time-previous)));
		previous = time;*/
		
		Graphics2D g2 = bImageFromConvert.createGraphics();
		VisualizeFeatures.drawPoint(g2, (int)location.a.x, (int)location.a.y, Color.RED);
		VisualizeFeatures.drawPoint(g2, (int)location.b.x, (int)location.b.y, Color.RED);
		VisualizeFeatures.drawPoint(g2, (int)location.c.x, (int)location.c.y, Color.RED);
		VisualizeFeatures.drawPoint(g2, (int)location.d.x, (int)location.d.y, Color.RED);
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		//BufferedImage outScaled = new BufferedImage(frame.width,frame.height,BufferedImage.TYPE_INT_RGB);
		ImageIO.write(bImageFromConvert, "jpg", baos);
		byte[] bytes = baos.toByteArray();
		return bytes;
		
	}
}
