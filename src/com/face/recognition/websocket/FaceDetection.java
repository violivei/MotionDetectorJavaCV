package com.face.recognition.websocket;

import javax.imageio.ImageIO;
import javax.servlet.ServletContext;

import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.javacpp.Loader;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.bytedeco.javacv.OpenCVFrameConverter;

import static org.bytedeco.javacpp.opencv_core.*;
import static org.bytedeco.javacpp.opencv_imgproc.*;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;

import static org.bytedeco.javacpp.opencv_imgcodecs.*;

//import org.bytedeco.javacpp.opencv_core.IplImage;
//import org.bytedeco.javacpp.opencv_objdetect.CvHaarClassifierCascade;
//import org.bytedeco.javacv.Frame;
//import org.bytedeco.javacv.Java2DFrameConverter;
//import org.bytedeco.javacv.OpenCVFrameConverter;
//import static org.bytedeco.javacpp.opencv_core.*;
//import static org.bytedeco.javacpp.opencv_imgcodecs.*;
//import static org.bytedeco.javacpp.opencv_imgproc.CV_AA;
//import static org.bytedeco.javacpp.opencv_imgproc.cvCvtColor;
//import static org.bytedeco.javacpp.opencv_objdetect.cvHaarDetectObjects;
//import static org.bytedeco.javacpp.opencv_imgproc.*;
//import java.awt.image.BufferedImage;
//import java.io.ByteArrayOutputStream;
//import java.io.IOException;
//import javax.imageio.ImageIO;
//import javax.servlet.ServletContext;
//import org.bytedeco.javacpp.BytePointer;
//import static org.bytedeco.javacpp.opencv_highgui.cvDecodeImage;// opencv 2.4

public class FaceDetection {

	public byte[] convert(byte[] imageData, ServletContext servletContext) throws Exception {

			IplImage originalImage = cvDecodeImage(cvMat(1, imageData.length,CV_8UC1, new BytePointer(imageData)));

			System.out.println("Convert to grayscale for recognition");
			IplImage grayImage = IplImage.create(originalImage.width(), originalImage.height(), IPL_DEPTH_8U, 1);
			
			CvMemStorage storage=CvMemStorage.create();
            CvSeq squares = new CvContour();
            squares = cvCreateSeq(0, Loader.sizeof(CvContour.class), Loader.sizeof(CvSeq.class), storage);
            
            cvCvtColor(originalImage, grayImage, CV_BGR2GRAY);
            cvThreshold(grayImage, grayImage, 127, 255, CV_THRESH_BINARY);
            cvFindContours(grayImage, storage, squares, Loader.sizeof(CvContour.class), CV_RETR_EXTERNAL, CV_CHAIN_APPROX_SIMPLE);
            CvSeq ss=null;
            for (int i=0; i<1; i++)
            {
                cvDrawContours(grayImage, squares, CvScalar.WHITE, CV_RGB(248, 18, 18), 1, -1, 8);
                ss=cvApproxPoly(squares, Loader.sizeof(CvContour.class), storage, CV_POLY_APPROX_DP, 8, 0);
            }

			ByteArrayOutputStream bout = new ByteArrayOutputStream();
			
			OpenCVFrameConverter.ToIplImage grabberConverter = new OpenCVFrameConverter.ToIplImage();
		    Java2DFrameConverter paintConverter = new Java2DFrameConverter();
		    Frame frame = grabberConverter.convert(grayImage);
		    BufferedImage imgb = paintConverter.getBufferedImage(frame,1);
		    
			ImageIO.write(imgb, "png", bout);
	
			return bout.toByteArray();

	}
}