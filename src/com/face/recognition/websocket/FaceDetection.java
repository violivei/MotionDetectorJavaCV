package com.face.recognition.websocket;

import javax.imageio.ImageIO;
import javax.servlet.ServletContext;

import org.apache.log4j.Logger;
import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.javacpp.Loader;
import org.bytedeco.javacpp.opencv_core.CvSeq;
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
	private int squareQuantity = 0;
	
	public int getSquareQuantity() {
		return squareQuantity;
	}

	public void setSquareQuantity(int squareQuantity) {
		this.squareQuantity = squareQuantity;
	}

	public byte[] convert(byte[] imageData, ServletContext servletContext) throws Exception {

			IplImage originalImage = cvDecodeImage(cvMat(1, imageData.length,CV_8UC1, new BytePointer(imageData)));

			System.out.println("Convert to grayscale for recognition");
			IplImage grayImage = IplImage.create(originalImage.width(), originalImage.height(), IPL_DEPTH_8U, 1);
			IplImage binaryImage = IplImage.create(originalImage.width(), originalImage.height(), IPL_DEPTH_8U, 1);
			
			CvMemStorage storage=CvMemStorage.create();
            CvSeq squares = new CvContour();
            squares = cvCreateSeq(0, Loader.sizeof(CvContour.class), Loader.sizeof(CvSeq.class), storage);
            
            CvSeq insideSquares = new CvContour();
            insideSquares = cvCreateSeq(0, Loader.sizeof(CvContour.class), Loader.sizeof(CvSeq.class), storage);
            
            cvCvtColor(originalImage, grayImage, CV_BGR2GRAY);
            cvThreshold(grayImage, binaryImage, 127, 255, CV_THRESH_BINARY);
            cvFindContours(binaryImage, storage, squares, Loader.sizeof(CvContour.class), CV_RETR_TREE, CV_CHAIN_APPROX_SIMPLE );
                        
            
            //cvDrawContours(grayImage, squares, CvScalar.WHITE, CV_RGB(248, 18, 18), 1, -1, 8);
            IplImage cropped = null;
            IplImage grayCroppedImage = null;
            IplImage tempImage;
            int i = 0;
            //cvDrawContours(originalImage, squares, CvScalar.BLUE, CV_RGB(248, 18, 18), 1, -1, 8);

            this.logger.info("Contours: " + squares.total());
            
            while (squares != null && !squares.isNull()) {
            	
            	CvRect rect=cvBoundingRect(squares, 0);
            	int x=rect.x() + 10,y=rect.y() + 10,h=rect.height() - 20,w=rect.width() - 20;
            	
            	rect.x(x);
            	rect.y(y);
            	rect.height(h);
            	rect.width(w);
            	
            	double area = h*w;
            	this.logger.info("Area#" + i + " :" + area);
            	
            	this.logger.info(servletContext.getRealPath("/WEB-INF/resources/teste" + i + ".jpg"));
            	
            	if(area > 10000){
	            	if(detectObject(originalImage, grayImage, rect, storage, servletContext.getRealPath("/WEB-INF/resources/teste" + i), i, area)) {
	            		cvRectangle(originalImage, cvPoint(x, y), cvPoint(x+w, y+h), CvScalar.BLUE, 1, CV_AA, 0);
	            		i++;
	            	} else {
	            		cvRectangle(originalImage, cvPoint(x, y), cvPoint(x+w, y+h), CvScalar.RED, 1, CV_AA, 0);
	            	}	            	
            	}
            	
            	
//            	
//            	
//            	cvSetImageROI(grayImage, rect);
//            	cropped = cvCreateImage(cvGetSize(grayImage), grayImage.depth(), grayImage.nChannels());
//            	// Copy original image (only ROI) to the cropped image
//            	cvCopy(grayImage, cropped);
//                
//                
////                this.logger.info(servletContext.getRealPath("/WEB-INF/resources/teste" + i + ".jpg"));
////                cvSaveImage(servletContext.getRealPath("/WEB-INF/resources/teste" + i + ".jpg") , cropped);
////                i++;
//                grayCroppedImage = IplImage.create(cropped.width(), cropped.height(), IPL_DEPTH_8U, 1);                
////                cvCvtColor(cropped, grayCroppedImage, CV_BGR2GRAY);
//                cvThreshold(cropped, grayCroppedImage, 127, 255, CV_THRESH_BINARY);
//////                
//                CvSeq innerSquares = new CvContour();
//                innerSquares = cvCreateSeq(0, Loader.sizeof(CvContour.class), Loader.sizeof(CvSeq.class), storage);
//////                
//                cvFindContours(grayCroppedImage, storage, innerSquares, Loader.sizeof(CvContour.class), CV_RETR_TREE, CV_CHAIN_APPROX_SIMPLE );
//////                
////                //cvDrawContours(grayImage, innerSquares, CvScalar.WHITE, CV_RGB(248, 18, 18), 1, -1, 8);
////                
//                cvDrawContours(originalImage, innerSquares, CvScalar.BLUE, CV_RGB(248, 18, 18), 1, -1, 8);
//                
//                while (innerSquares != null && !innerSquares.isNull()) {                	
////                	CvRect rect2=cvBoundingRect(innerSquares, 0);
////                	int x2=rect2.x(),y2=rect2.y(),h2=rect2.height(),w2=rect2.width();
////                	cvRectangle(originalImage, cvPoint(x, y), cvPoint(x+w, y+h), CvScalar.BLUE, 1, CV_AA, 0);
//                	innerSquares=innerSquares.h_next();               	
//                }
//                
//                cvResetImageROI(grayImage); 
            	squares=squares.h_next();
            }
            squareQuantity = i;
            this.logger.info("Squares: " + i);
            
            
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
		    Frame frame = grabberConverter.convert(originalImage/*grayCroppedImage == null ? grayImage : grayCroppedImage*/);
		    BufferedImage imgb = paintConverter.getBufferedImage(frame,1);
		    
			ImageIO.write(imgb, "png", bout);
	
			return bout.toByteArray();

	}
	
	public boolean detectObject(IplImage original, IplImage grayImage, CvRect rect, CvMemStorage storage, String path, int i, double parentArea){
		
    	
		cvSetImageROI(original, rect);
    	cvSetImageROI(grayImage, rect);
    	IplImage cropped = cvCreateImage(cvGetSize(grayImage), grayImage.depth(), grayImage.nChannels());
    	// Copy original image (only ROI) to the cropped image
    	cvCopy(grayImage, cropped);

    	IplImage binaryCropedImage = IplImage.create(cropped.width(), cropped.height(), IPL_DEPTH_8U, 1);  
        cvThreshold(cropped, binaryCropedImage, 127, 255, CV_THRESH_BINARY);
       
        CvSeq innerSquares = new CvContour();
        innerSquares = cvCreateSeq(0, Loader.sizeof(CvContour.class), Loader.sizeof(CvSeq.class), storage);
      
        cvFindContours(binaryCropedImage, storage, innerSquares, Loader.sizeof(CvContour.class), CV_RETR_TREE, CV_CHAIN_APPROX_SIMPLE );
	    
        CvSeq approx = cvApproxPoly(innerSquares, Loader.sizeof(CvContour.class),storage, CV_POLY_APPROX_DP, cvContourPerimeter(innerSquares)*0.02, 0);
                
        double innerArea = Math.abs(cvContourArea(approx, CV_WHOLE_SEQ, 0));
        double rate = innerArea/parentArea;
        
//        while (innerSquares != null && !innerSquares.isNull()) {                	
////        	CvRect rect2=cvBoundingRect(innerSquares, 0);
////        	int x2=rect2.x(),y2=rect2.y(),h2=rect2.height(),w2=rect2.width();
////        	cvRectangle(originalImage, cvPoint(x, y), cvPoint(x+w, y+h), CvScalar.BLUE, 1, CV_AA, 0);
//        	innerSquares=innerSquares.h_next();               	
//        }
//        
        
        cvSaveImage(path  + ".jpg" , cropped);
        cvDrawContours(grayImage, innerSquares, CvScalar.WHITE, CV_RGB(248, 18, 18), 1, -1, 8);
	    cvSaveImage(path  + "Binary.jpg" , original);
	    	    
        cvResetImageROI(original); 
        cvResetImageROI(grayImage); 
        
        i++;
		
        if(rate < 0.001){
        	return false;
        }else{
        	return true;
        }
		
	}
	
}