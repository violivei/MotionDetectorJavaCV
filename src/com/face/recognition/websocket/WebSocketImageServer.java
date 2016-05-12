package com.face.recognition.websocket;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.websocket.EndpointConfig;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import org.apache.log4j.Logger;

@ServerEndpoint(value = "/imageEcho", configurator = GetHttpSessionConfigurator.class)
public class WebSocketImageServer {

	private Logger logger = Logger.getLogger(WebSocketImageServer.class);
	private static Set<Session> clients = Collections.synchronizedSet(new HashSet<Session>());
	private ExampleTrackerObjectQuad faceDetection;
	private HttpSession httpSession;
	private String classifierName;
	private boolean firstImage = true;

	@OnOpen
	public void onOpen (Session session, EndpointConfig config) throws IOException {
        this.httpSession = (HttpSession) config.getUserProperties()
                .get(HttpSession.class.getName());
		// Set max buffer size
		session.setMaxBinaryMessageBufferSize(1024*512);
		// Add session to the connected sessions set
		clients.add(session);
		this.logger.info("Client " + session.getId() + " connected.");
		
        /*URL url = new URL("https://raw.github.com/Itseez/opencv/2.4.0/data/haarcascades/haarcascade_frontalface_alt.xml");
        File file = Loader.extractResource(url, null, "classifier", ".xml");
        file.deleteOnExit();
        this.classifierName = file.getAbsolutePath();*/
		
	}

	@OnClose
	public void onClose (Session session) {
		// Remove session from the connected sessions set
		clients.remove(session);
		this.logger.info("Client " + session.getId() + " disconnected.");
	}

	@OnError
	public void onError(Throwable error) {
		this.logger.error(error.getMessage());
	}

	@OnMessage
	public void processVideo(byte[] imageData, Session session) throws Exception {
		
		try {

			if (session.isOpen()) {
		        
		        ServletContext servletContext = this.httpSession.getServletContext();
				// Wrap a byte array into a buffer		
		        byte[] result;
		        System.out.println("Wrap a byte array into a buffer");
		        
		        if(firstImage) {
		        	faceDetection = new ExampleTrackerObjectQuad(imageData);
		        	//result = faceDetection.getVisualized();
		        	result = imageData;
		        	firstImage = false;
		        } else {
		        	result = faceDetection.convert(imageData);
		        }
				
				ByteBuffer buf = ByteBuffer.wrap(result, 0, result.length);
				session.getBasicRemote().sendBinary(buf);
			}

		} catch (IOException e) {
			try {
				session.close();
			} catch (IOException e1) {
			}
		}
	}

}
