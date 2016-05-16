package com.face.recognition.websocket;

import javax.imageio.ImageIO;
import javax.servlet.ServletContext;

import org.apache.log4j.Logger;
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

	private Logger logger = Logger.getLogger(WebSocketImageServer.class);
	
	public byte[] convert(byte[] imageData, ServletContext servletContext) throws Exception {

			IplImage originalImage = cvDecodeImage(cvMat(1, imageData.length,CV_8UC1, new BytePointer(imageData)));

			System.out.println("Convert to grayscale for recognition");
			IplImage grayImage = IplImage.create(originalImage.width(), originalImage.height(), IPL_DEPTH_8U, 1);
			
			CvMemStorage storage=CvMemStorage.create();
            CvSeq squares = new CvContour();
            squares = cvCreateSeq(0, Loader.sizeof(CvContour.class), Loader.sizeof(CvSeq.class), storage);
            
            CvSeq insideSquares = new CvContour();
            insideSquares = cvCreateSeq(0, Loader.sizeof(CvContour.class), Loader.sizeof(CvSeq.class), storage);
            
            cvCvtColor(originalImage, grayImage, CV_BGR2GRAY);
            cvThreshold(grayImage, grayImage, 127, 255, CV_THRESH_BINARY);
            cvFindContours(grayImage, storage, squares, Loader.sizeof(CvContour.class), CV_RETR_EXTERNAL, CV_CHAIN_APPROX_SIMPLE);
            
            this.logger.info("Contours: " + squares.total());
            
            //cvDrawContours(grayImage, squares, CvScalar.WHITE, CV_RGB(248, 18, 18), 1, -1, 8);
            
            while (squares != null && !squares.isNull()) {
            	
                CvRect rect=cvBoundingRect(squares, 0);
                int x=rect.x(),y=rect.y(),h=rect.height(),w=rect.width();
                cvRectangle(grayImage, cvPoint(x, y), cvPoint(x+w, y+h), CvScalar.WHITE, 1, CV_AA, 0);
//                int x=rect.x(),y=rect.y(),h=rect.height(),w=rect.width();
//                if (10 < w/h || w/h < 0.1){
//                    cvRectangle(grayImage, cvPoint(x, y), cvPoint(x+w, y+h), CvScalar.RED, 1, CV_AA, 0);
//                }
                squares=squares.h_next();
            }
        
            
            
            //            CvSeq ss=null;
//            CvSeq sa=null;
//            
//            cvDrawContours(grayImage, squares, CvScalar.WHITE, CV_RGB(248, 18, 18), 1, -1, 8);
//   
//            for (int j=0; j<contourQuantity; j++)
//            {
//            	ss=cvApproxPoly(squares, Loader.sizeof(CvContour.class), storage, CV_POLY_APPROX_DP, 8, 0);
//            	//cvContourArea(squares);
//            	cvFindContours(squares, storage, insideSquares, Loader.sizeof(CvContour.class), CV_RETR_EXTERNAL, CV_CHAIN_APPROX_SIMPLE);
//                
//            	cvDrawContours(squares, insideSquares, CvScalar.BLACK, CV_RGB(248, 18, 18), 1, -1, 8);
//            	//sa=cvApproxPoly(squares, Loader.sizeof(CvContour.class), storage, CV_POLY_APPROX_DP, 8, 0);
//            }
            

			ByteArrayOutputStream bout = new ByteArrayOutputStream();
			
			OpenCVFrameConverter.ToIplImage grabberConverter = new OpenCVFrameConverter.ToIplImage();
		    Java2DFrameConverter paintConverter = new Java2DFrameConverter();
		    Frame frame = grabberConverter.convert(grayImage);
		    BufferedImage imgb = paintConverter.getBufferedImage(frame,1);
		    
			ImageIO.write(imgb, "png", bout);
	
			return bout.toByteArray();

	}
}