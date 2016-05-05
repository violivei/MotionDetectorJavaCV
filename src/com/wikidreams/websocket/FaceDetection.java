package com.wikidreams.websocket;

//import static org.bytedeco.javacpp.opencv_core.*; //cvMat
//import static org.bytedeco.javacpp.opencv_imgcodecs.*;
//import static com.googlecode.javacv.cpp.opencv_imgproc.*;
//
//import static com.googlecode.javacv.cpp.opencv_calib3d.*;
//import static com.googlecode.javacv.cpp.opencv_objdetect.*;

//import com.googlecode.javacpp.Loader;
////import com.googlecode.javacv.*;
//import com.googlecode.javacv.cpp.*;
//import static com.googlecode.javacv.cpp.opencv_core.*;
//import static com.googlecode.javacv.cpp.opencv_imgproc.*;
//import static com.googlecode.javacv.cpp.opencv_calib3d.*;
//import static com.googlecode.javacv.cpp.opencv_objdetect.*;
//import static org.bytedeco.javacpp.opencv_core.cvMat;
//import static com.googlecode.javacv.cpp.opencv_highgui.cvDecodeImage;//cvDecodeImage
//import static com.googlecode.javacv.cpp.opencv_core.CV_8UC1;
//import static org.bytedeco.javacpp.opencv_highgui.*;

//import static com.googlecode.javacv.cpp.opencv_core.CV_8UC1;
//import static com.googlecode.javacv.cpp.opencv_core.cvMat;
//import static com.googlecode.javacv.cpp.opencv_highgui.cvDecodeImage;

//import static org.bytedeco.javacpp.opencv_imgproc.CV_BGR2GRAY;

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
import static org.bytedeco.javacpp.opencv_highgui.*;
import static org.bytedeco.javacpp.opencv_imgproc.*;


import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.RenderedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import javax.imageio.ImageIO;

import org.apache.log4j.Logger;
import org.bytedeco.javacpp.BytePointer;


public class FaceDetection {
	 
	private static final String CASCADE_FILE = "C:/Users/victor.o.antonino/workspace/JavaScript-WebSocket/haarcascade_frontalface_default.xml";
	private Logger logger = Logger.getLogger(WebSocketImageServer.class);
	private int minsize = 20;
	private int group = 0;
	private double scale = 1.1;
 
	/**
	 * Based on FaceDetection example from JavaCV.
	 */
	public byte[] convert(byte[] imageData) throws IOException {
		// create image from supplied bytearray
		
		IplImage originalImage = cvDecodeImage(cvMat(1, imageData.length,CV_8UC1, new BytePointer(imageData)));
		
		// Convert to grayscale for recognition
		IplImage grayImage = IplImage.create(originalImage.width(), originalImage.height(), IPL_DEPTH_8U, 1);
		cvCvtColor(originalImage, grayImage, CV_BGR2GRAY);
		
		// storage is needed to store information during detection
		CvMemStorage storage = CvMemStorage.create();
 
		// Configuration to use in analysis
		CvHaarClassifierCascade cascade = new CvHaarClassifierCascade(cvLoad(CASCADE_FILE));
 
		// We detect the faces.
		CvSeq faces = cvHaarDetectObjects(grayImage, cascade, storage, scale, group, minsize);
 
		// We iterate over the discovered faces and draw yellow rectangles around them.
		for (int i = 0; i < faces.total(); i++) {
			CvRect r = new CvRect(cvGetSeqElem(faces, i));
			cvRectangle(originalImage, cvPoint(r.x(), r.y()),
					cvPoint(r.x() + r.width(), r.y() + r.height()),
					CvScalar.YELLOW, 1, CV_AA, 0);
		}
		
		// convert the resulting image back to an array
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		
	    OpenCVFrameConverter.ToIplImage grabberConverter = new OpenCVFrameConverter.ToIplImage();
	    Java2DFrameConverter paintConverter = new Java2DFrameConverter();
	    Frame frame = grabberConverter.convert(originalImage);
	    BufferedImage imgb = paintConverter.getBufferedImage(frame,1);
	    
		ImageIO.write(imgb, "png", bout);
		return bout.toByteArray();
	}
}