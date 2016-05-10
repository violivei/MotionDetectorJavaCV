package com.face.recognition.websocket;

import org.bytedeco.javacpp.opencv_core.IplImage;
import org.bytedeco.javacpp.opencv_objdetect.CvHaarClassifierCascade;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.bytedeco.javacv.OpenCVFrameConverter;
import static org.bytedeco.javacpp.opencv_core.*;
import static org.bytedeco.javacpp.opencv_imgcodecs.*;
import static org.bytedeco.javacpp.opencv_imgproc.CV_BGR2GRAY;
import static org.bytedeco.javacpp.opencv_imgproc.CV_AA;
import static org.bytedeco.javacpp.opencv_imgproc.cvCvtColor;
import static org.bytedeco.javacpp.opencv_objdetect.cvHaarDetectObjects;
import static org.bytedeco.javacpp.opencv_imgproc.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.servlet.ServletContext;
import org.bytedeco.javacpp.BytePointer;
 

public class FaceDetection {
	 
	private int minsize = 20;
	private int group = 0;
	private double scale = 1.1;
 
	/**
	 * Based on FaceDetection example from JavaCV.
	 */
	public byte[] convert(byte[] imageData, ServletContext servletContext) throws Exception {
		// create image from supplied bytearray
		try{
			
			System.out.println("create image from supplied bytearray");
			String filename = servletContext.getRealPath("/WEB-INF/resources/haarcascade_frontalface_default.xml");
			
			IplImage originalImage = cvDecodeImage(cvMat(1, imageData.length,CV_8UC1, new BytePointer(imageData)));
								
			// Convert to grayscale for recognition
			System.out.println("Convert to grayscale for recognition");
			IplImage grayImage = IplImage.create(originalImage.width(), originalImage.height(), IPL_DEPTH_8U, 1);
			cvCvtColor(originalImage, grayImage, CV_BGR2GRAY);
			
			// storage is needed to store information during detection
			System.out.println("storage is needed to store information during detection");
			CvMemStorage storage = CvMemStorage.create();
	 
			// Configuration to use in analysis
			System.out.println("Configuration to use in analysis");
			CvHaarClassifierCascade cascade = new CvHaarClassifierCascade(cvLoad(filename));
	 
			// We detect the faces.
			System.out.println("We detect the faces.");
			CvSeq faces = cvHaarDetectObjects(grayImage, cascade, storage, scale, group, minsize);
	 
			// We iterate over the discovered faces and draw yellow rectangles around them.
			System.out.println("We iterate over the discovered faces and draw yellow rectangles around them.");
			for (int i = 0; i < faces.total(); i++) {
				CvRect r = new CvRect(cvGetSeqElem(faces, i));
				cvRectangle(originalImage, cvPoint(r.x(), r.y()),
						cvPoint(r.x() + r.width(), r.y() + r.height()),
						CvScalar.YELLOW, 1, CV_AA, 0);
			}
			
			// convert the resulting image back to an array
			System.out.println("convert the resulting image back to an array");
			ByteArrayOutputStream bout = new ByteArrayOutputStream();
			
		    OpenCVFrameConverter.ToIplImage grabberConverter = new OpenCVFrameConverter.ToIplImage();
		    Java2DFrameConverter paintConverter = new Java2DFrameConverter();
		    Frame frame = grabberConverter.convert(originalImage);
		    BufferedImage imgb = paintConverter.getBufferedImage(frame,1);
		    
			ImageIO.write(imgb, "png", bout);
	
			return bout.toByteArray();
		
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
}