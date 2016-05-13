package com.face.recognition.websocket;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

import org.ddogleg.struct.FastQueue;
import org.ddogleg.struct.GrowQueue_I32;

import boofcv.abst.distort.FDistort;
import boofcv.abst.flow.DenseOpticalFlow;
import boofcv.abst.segmentation.ImageSuperpixels;
import boofcv.alg.color.ColorHsv;
import boofcv.alg.filter.blur.GBlurImageOps;
import boofcv.alg.segmentation.ComputeRegionMeanColor;
import boofcv.alg.segmentation.ImageSegmentationOps;
import boofcv.factory.flow.FactoryDenseOpticalFlow;
import boofcv.factory.segmentation.ConfigFh04;
import boofcv.factory.segmentation.FactoryImageSegmentation;
import boofcv.factory.segmentation.FactorySegmentationAlg;
import boofcv.gui.PanelGridPanel;
import boofcv.gui.feature.VisualizeOpticalFlow;
import boofcv.gui.feature.VisualizeRegions;
import boofcv.gui.image.AnimatePanel;
import boofcv.gui.image.ShowImages;
import boofcv.io.image.ConvertBufferedImage;
import boofcv.struct.feature.ColorQueue_F32;
import boofcv.struct.flow.ImageFlow;
import boofcv.struct.image.GrayF32;
import boofcv.struct.image.GrayS32;
import boofcv.struct.image.ImageBase;
import boofcv.struct.image.ImageType;
import boofcv.struct.image.Planar;
import georegression.metric.UtilAngle;

public class ExampleDenseOpticalFlow {
	
	ExampleDenseOpticalFlow(){};
	
	public byte[] convert(byte[] imageData) throws IOException{
		
		InputStream in = new ByteArrayInputStream(imageData);
		BufferedImage image = ImageIO.read(in);
		//float hue = 1f;
		//float saturation = 1f;//Green
		
		ImageType<Planar<GrayF32>> imageType = ImageType.pl(3, GrayF32.class);
		ImageSuperpixels alg = FactoryImageSegmentation.fh04(new ConfigFh04(1000,1000), imageType);
		
		ImageBase color = imageType.createImage(image.getWidth(),image.getHeight());
		ConvertBufferedImage.convertFrom(image, color, true);
		
		//performSegmentation
		
		// Segmentation often works better after blurring the image.  Reduces high frequency image components which
		// can cause over segmentation
		GBlurImageOps.gaussian(color, color, 0.5, -1, null);
 
		// Storage for segmented image.  Each pixel will be assigned a label from 0 to N-1, where N is the number
		// of segments in the image
		GrayS32 pixelToSegment = new GrayS32(color.width,color.height);
 
		// Segmentation magic happens here
		alg.segment(color,pixelToSegment);
		
		// Displays the results
		int numSegments = alg.getTotalSuperpixels();
		
		// Computes the mean color inside each region
		ImageType type = color.getImageType();
		ComputeRegionMeanColor colorize = FactorySegmentationAlg.regionMeanColor(type);
 
		FastQueue<float[]> segmentColor = new ColorQueue_F32(type.getNumBands());
		segmentColor.resize(numSegments);
 
		GrowQueue_I32 regionMemberCount = new GrowQueue_I32();
		regionMemberCount.resize(numSegments);
 
		ImageSegmentationOps.countRegionPixels(pixelToSegment, numSegments, regionMemberCount.data);
		colorize.process(color,pixelToSegment,regionMemberCount,segmentColor);
		
		// Draw each region using their average color
		BufferedImage outColor = VisualizeRegions.regionsColor(pixelToSegment,segmentColor,null);
		// Draw each region by assigning it a random color
		BufferedImage outSegments = VisualizeRegions.regions(pixelToSegment, numSegments, null);
 
		// Make region edges appear red
		BufferedImage outBorder = new BufferedImage(color.width,color.height,BufferedImage.TYPE_INT_RGB);
		ConvertBufferedImage.convertTo(color, outBorder, true);
		VisualizeRegions.regionBorders(pixelToSegment,0xFF0000,outBorder);
 
		// Show the visualization results
//		ListDisplayPanel gui = new ListDisplayPanel();
//		gui.addImage(outColor,"Color of Segments");
//		gui.addImage(outBorder, "Region Borders");
//		gui.addImage(outSegments, "Regions");
//		ShowImages.showWindow(gui,"Superpixels", true);
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ImageIO.write(outSegments, "jpg", baos);
		byte[] bytes = baos.toByteArray();
		return bytes;
		
		////////////////////////////////////////////////////////////////////////////////////////////////////
		
		/*Planar<GrayF32> input = ConvertBufferedImage.convertFromMulti(image,null,true,GrayF32.class);
		Planar<GrayF32> hsv = input.createSameShape();
 
		// Convert into HSV
		ColorHsv.rgbToHsv_F32(input,hsv);
 
		// Euclidean distance squared threshold for deciding which pixels are members of the selected set
		float maxDist2 = 0.4f*0.4f;
 
		// Extract hue and saturation bands which are independent of intensity
		GrayF32 H = hsv.getBand(0);
		GrayF32 S = hsv.getBand(1);
 
		// Adjust the relative importance of Hue and Saturation.
		// Hue has a range of 0 to 2*PI and Saturation from 0 to 1.
		float adjustUnits = (float)(Math.PI/2.0);
 
		// step through each pixel and mark how close it is to the selected color
		BufferedImage output = new BufferedImage(input.width,input.height,BufferedImage.TYPE_INT_RGB);
		for( int y = 0; y < hsv.height; y++ ) {
			for( int x = 0; x < hsv.width; x++ ) {				
				// Hue is an angle in radians, so simple subtraction doesn't work
				float dh = UtilAngle.dist(H.unsafe_get(x,y),hue);
				float ds = (S.unsafe_get(x,y)-saturation)*adjustUnits;
 
				// this distance measure is a bit naive, but good enough for to demonstrate the concept
				float dist2 = dh*dh + ds*ds;
				if( dist2 <= maxDist2 ) {
					output.setRGB(x,y,image.getRGB(x,y));
				}
			}
		}
 
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ImageIO.write(output, "jpg", baos);
		byte[] bytes = baos.toByteArray();
		return bytes;*/
		
	}
	
	
//	GrayF32 previous, current;
//	ImageFlow flow;
//	BufferedImage buff0;
//	GrayF32 full;
//	DenseOpticalFlow<GrayF32> denseFlow;
//	
//	ExampleDenseOpticalFlow(byte[] imageData) throws IOException{
//		
//		InputStream in = new ByteArrayInputStream(imageData);
//		buff0 = ImageIO.read(in);
//		
//		denseFlow =
////				FactoryDenseOpticalFlow.flowKlt(null, 6, GrayF32.class, null);
////				FactoryDenseOpticalFlow.region(null,GrayF32.class);
////				FactoryDenseOpticalFlow.hornSchunck(20, 1000, GrayF32.class);
////				FactoryDenseOpticalFlow.hornSchunckPyramid(null,GrayF32.class);
//				FactoryDenseOpticalFlow.broxWarping(null, GrayF32.class);
//		
//		full = new GrayF32(buff0.getWidth(),buff0.getHeight());
//		 
//		// Dense optical flow is very computationally expensive.  Just process the image at 1/2 resolution
//		previous = new GrayF32(full.width/4,full.height/4);
//		current = previous.createSameShape();
//		flow = new ImageFlow(previous.width,previous.height);
//	}
//	
//	public byte[] convert(byte[] imageData) throws IOException{
//		
//		InputStream in = new ByteArrayInputStream(imageData);
//		BufferedImage buff1 = ImageIO.read(in);
//		
//		ConvertBufferedImage.convertFrom(buff0,full);
//		new FDistort(full, previous).scaleExt().apply();
//		ConvertBufferedImage.convertFrom(buff1, full);
//		new FDistort(full, current).scaleExt().apply();
// 
//		// compute dense motion
//		denseFlow.process(previous, current, flow);
// 
//		// Visualize the results
//		PanelGridPanel gui = new PanelGridPanel(1,2);
// 
//		BufferedImage converted0 = new BufferedImage(current.width,current.height,BufferedImage.TYPE_INT_RGB);
//		BufferedImage converted1 = new BufferedImage(current.width,current.height,BufferedImage.TYPE_INT_RGB);
//		BufferedImage visualized = new BufferedImage(current.width,current.height,BufferedImage.TYPE_INT_RGB);
// 
//		ConvertBufferedImage.convertTo(previous, converted0, true);
//		ConvertBufferedImage.convertTo(current, converted1, true);
//		VisualizeOpticalFlow.colorized(flow, 10, visualized);
//// 
////		AnimatePanel animate = new AnimatePanel(150,converted0,converted1);
////		gui.add(animate);
////		gui.add(visualized);
////		animate.start();
//// 
////		ShowImages.showWindow(gui,"Dense Optical Flow",true);
//		
//		//previous = current;
//		ByteArrayOutputStream baos = new ByteArrayOutputStream();
//		ImageIO.write(visualized, "jpg", baos);
//		byte[] bytes = baos.toByteArray();
//		return bytes;
//		
//	}
//	
}
