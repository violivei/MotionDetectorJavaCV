package com.wikidreams.websocket;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import org.apache.log4j.Logger;

@ServerEndpoint("/imageEcho")
public class WebSocketImageServer {

	private Logger logger = Logger.getLogger(WebSocketImageServer.class);
	private static Set<Session> clients = Collections.synchronizedSet(new HashSet<Session>());
	private FaceDetection faceDetection = new FaceDetection();

	@OnOpen
	public void onOpen (Session session) {
		// Set max buffer size
		session.setMaxBinaryMessageBufferSize(1024*512);
		// Add session to the connected sessions set
		clients.add(session);
		this.logger.info("Client " + session.getId() + " connected.");
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
	public void processVideo(byte[] imageData, Session session) {
		this.logger.info("processVideo " + imageData.length);
		try {

			if (session.isOpen()) {
				// Wrap a byte array into a buffer
				
				this.logger.info("convert " + imageData.length);
				byte[] result = faceDetection.convert(imageData);				
				this.logger.info("return " + imageData.length);
				
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
