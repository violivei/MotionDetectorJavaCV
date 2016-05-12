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
import boofcv.gui.feature.VisualizeFeatures;
import boofcv.io.image.ConvertBufferedImage;
import boofcv.struct.image.GrayU8;
import boofcv.struct.image.ImageBase;
import boofcv.struct.image.ImageType;
import georegression.struct.shapes.Quadrilateral_F64;

public class ExampleTrackerObjectQuad {
	
	private ImageBase frame;
	private TrackerObjectQuad tracker;
	long previous = 0;
	Quadrilateral_F64 location;

	ExampleTrackerObjectQuad(byte[] imageData) throws IOException{

		// Create the tracker.  Comment/Uncomment to change the tracker.
		tracker = FactoryTrackerObjectQuad.circulant(null, GrayU8.class);				

		// specify the target's initial location and initialize with the first frame
		location = new Quadrilateral_F64(200.0,200.0,200.0,400.0,400.0,200.0,400.0,400.0);
		
		InputStream in = new ByteArrayInputStream(imageData);
		BufferedImage bImageFromConvert = ImageIO.read(in);
		
		ImageType<GrayU8> imageType = ImageType.single(GrayU8.class);
		ImageBase frame = imageType.createImage(bImageFromConvert.getWidth(),bImageFromConvert.getHeight());
		ConvertBufferedImage.convertFrom(bImageFromConvert, frame, true);
		
		tracker.initialize(frame,location);

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
		
		Graphics2D g2 = bImageFromConvert.createGraphics();
		VisualizeFeatures.drawPoint(g2, (int)location.a.x, (int)location.a.y, Color.RED);
		VisualizeFeatures.drawPoint(g2, (int)location.b.x, (int)location.b.y, Color.RED);
		VisualizeFeatures.drawPoint(g2, (int)location.c.x, (int)location.c.y, Color.RED);
		VisualizeFeatures.drawPoint(g2, (int)location.d.x, (int)location.d.y, Color.RED);
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ImageIO.write(bImageFromConvert, "jpg", baos);
		byte[] bytes = baos.toByteArray();
		return bytes;
		
	}
}
