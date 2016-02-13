# WebSocket

## WebSockets is a technology, based on the ws protocol, that makes it possible to establish a continuous full-duplex connection stream between a client and a server.  A typical websocket client would be a user's browser, but the protocol is platform independent.

Project goal:


1. Transfer a String through a WebSocket using a JavaScript client and a Java server.
2. Transfer a image file through a WebSocket using a JavaScript client and a Java server.
3. Transfer a String converted in binary through a WebSocket using a JavaScript client and a Java server.


## This is a maven project so first we have to add the dependencies.
The first one enables Web Socket communication and the another one it is just to create a logging system to the server side.
Add them to your pom.xml file.


	<dependencies>
		<!-- Log4j -->
		<dependency>
			<groupId>log4j</groupId>
			<artifactId>log4j</artifactId>
			<version>1.2.16</version>
		</dependency>
	
		<!-- WebSocket -->
	<dependency>
				<groupId>javax</groupId>
				<artifactId>javaee-api</artifactId>
				<version>7.0</version>
			</dependency>
		</dependencies>


## 1. Transfer a String through a WebSocket using a JavaScript client and a Java server.

	
## Now lets create the server side class (WebSocketStringServer).
	
	
		package com.wikidreams.websocket;

	import java.io.IOException;
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

	@ServerEndpoint("/echo")
	public class WebSocketStringServer {

		private Logger logger = Logger.getLogger(WebSocketStringServer.class);
		private static Set<Session> clients = Collections.synchronizedSet(new HashSet<Session>());


		@OnOpen
		public void onOpen (Session session) {
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
		public void echoTextMessage(Session session, String msg, boolean last) {
			try {
				if (session.isOpen()) {
					session.getBasicRemote().sendText(msg, last);
				}
			} catch (IOException e) {
				try {
					session.close();
				} catch (IOException e1) {
				}
			}
		}

	}

	
## Now is time to create the client side.
	
	
## HTML page.


	<!DOCTYPE html>
	<html>
	<head>
	<meta charset="ISO-8859-1">
	<title>WebSocket Demo</title>
	</head>
	<body>

        	<h1 style="text-align: center;">WebSocket Client</h2><br>

	<h2>Example using Strings.</h2>
  
        	<div id="stringContainer" style="text-align: center;">
	<button id="connectBtn">Connect</button>
	<button id="disconnectBtn">Disconnect</button>
	<button id="sendMessageBtn">Send Message</button>
	<input id="textID" name="message" value="Hello WebSocket!" type="text"><br>
	<div id="output"></div>
	</div><br />

	<script type="text/javascript"src="WebSocketStringClient.js"></script>

	</body>
	</html>


## Lets create the Web socket on the client side (WebSocketStringClient.js).


	/* WebSocket - Sending and receiving Strings */

	var wsUri = null;
	var websocket = null;

	function init() {
		output = document.getElementById("output");
	}

	var connectBtn = document.getElementById("connectBtn");
	connectBtn.onclick = function() {
		if (websocket == null) {
			// Server address.
			wsUri = "ws://localhost:8081/WebSocket/echo";

			// Create WebSocket.
			websocket = new WebSocket(wsUri);

			websocket.onopen = function(evt) {
				onOpen(evt)
			};

			websocket.onmessage = function(evt) {
				onMessage(evt)
			};

			websocket.onerror = function(evt) {
				onError(evt)
			};
		}
	};

	function onOpen(evt) {
		writeToScreen("Connected to Endpoint!");
	}

	function onError(evt) {
		writeToScreen('ERROR: ' + evt.data);
	}

	function onMessage(evt) {
		writeToScreen("Message Received: " + evt.data);
	}

	var disconnectBtn = document.getElementById("disconnectBtn");
	disconnectBtn.onclick = function() {
		if (websocket != null) {
			websocket.close();
			websocket = null;
			writeToScreen("Disconnected from Endpoint!");	
		}
	};

	var sendMessageBtn = document.getElementById("sendMessageBtn");
	sendMessageBtn.onclick = function() {
		if (websocket != null) {
			var message = document.getElementById("textID").value;
			writeToScreen("Message Sent: " + message);
			websocket.send(message);		
		}
	};

	function writeToScreen(message) {
		var pre = document.createElement("p");
		pre.style.wordWrap = "break-word";
		pre.innerHTML = message;

		output.appendChild(pre);
	}

	window.addEventListener("load", init, false);


## Good job, you are donne the first example!


# Lets start the second example. Now the same process but with image files.


## 2. Transfer a image file through a WebSocket using a JavaScript client and a Java server.


## Server side class (WebSocketImageServer).
	
	
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
		try {

			if (session.isOpen()) {
				// Wrap a byte array into a buffer
				ByteBuffer buf = ByteBuffer.wrap(imageData);
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
	
	
## Now is time to create the client side.
	
	
## HTML page.


	<!DOCTYPE html>
	<html>
	<head>
	<meta charset="ISO-8859-1">
	<title>WebSocket Demo</title>
	</head>
	<body>

	<h2>Example using an image file.</h2><br />

	<div id="container" style="text-align: center;">
	<canvas id="image"></canvas>
	<button id="connectBtn">Connect</button>
	<button id="disconnectBtn">Disconnect</button>
	<button id="sendMessageBtn">Send Image File</button>
	<div id="output"></div>
	<canvas id="newImage"></canvas>
	</div>

	<script type="text/javascript"src="WebSocketImageClient.js"></script>

	</body>
	</html>


## Lets create the Web socket on the client side (WebSocketImageClient.js).


	/* WebSocket - Communication using buffer */

	/* Add an image to image canvas */

	var img = new Image();
	img.src = "images/webrtc-icon-192x192.png";
	var canvas = document.getElementById("image");
	var context = canvas.getContext("2d");
	img.onload = function () {
		context.drawImage(img, 0, 0);
	}


	/* Create the WebSocket */

	//Server address.
	var wsUri = "ws://localhost:8081/WebSocket/imageEcho";
	var websocket = null;

	//Connect button.
	var connectBtn = document.getElementById("connectBtn");
	connectBtn.onclick = function() {
		if (websocket == null) {
			// Create WebSocket.
			websocket = new WebSocket(wsUri);
			// Define type of WebSocket, can be UTF-8, buffer or blob.
			websocket.binaryType = 'arraybuffer';

			// Add WebSocket default methods.
			websocket.onopen = function(evt) {
				onOpen(evt)
			};

			websocket.onmessage = function(evt) {
				onMessage(evt)
			};

			websocket.onerror = function(evt) {
				onError(evt)
			};
		}
	};


	function onOpen(evt) {
		writeToScreen("Connected to Endpoint!");
	}

	function onError(evt) {
		writeToScreen('ERROR: ' + evt.data);
	}

	function onMessage(evt) {
		// Our WebSocket only accepts communication throw binary (buffer).
		if(evt.data instanceof ArrayBuffer) {
			convertFromBinary(evt.data);
			writeToScreen("File received.");
		}
	}


	function convertToBinary(image) {
		var buffer = new ArrayBuffer(image.data.length);
		var bytes = new Uint8Array(buffer);
		for (var i=0; i<bytes.length; i++) {
			bytes[i] = image.data[i];
		}
		return buffer;
	}

	function convertFromBinary(buffer) {
		var bytes = new Uint8Array(buffer);
		var canvas = document.getElementById("newImage");
		var context = canvas.getContext("2d");
		var image = context.createImageData(canvas.width, canvas.height);
		for (var i=0; i<bytes.length; i++) {
			image.data[i] = bytes[i];
		}
		//Add the new image to the newImage canvas.
		context.putImageData(image,0,0);
	}


	//Disconnect button.
	var disconnectBtn = document.getElementById("disconnectBtn");
	disconnectBtn.onclick = function() {
		if (websocket != null) {
			websocket.close();
			websocket = null;
			writeToScreen("Disconnected from Endpoint!");	
		}
	};


	var sendMessageBtn = document.getElementById("sendMessageBtn");
	sendMessageBtn.onclick = function() {
		if (websocket != null) {
			var image = context.getImageData(0, 0, canvas.width, canvas.height);
			var buffer = convertToBinary(image);
			websocket.send(buffer);
			writeToScreen("File sended.");
		}
	};


	/* Screen information */

	function writeToScreen(message) {
		var pre = document.createElement("p");
		pre.style.wordWrap = "break-word";
		pre.innerHTML = message;
		output.appendChild(pre);
	}

	//Update UI.
	function init() {
		output = document.getElementById("output");
	}


	window.addEventListener("load", init, false);


## Good job, you are donne the second example too!


## 3. Transfer a String converted in binary through a WebSocket using a JavaScript client and a Java server.


The Server side will be the same of the last one and the Client equals to the first one.
Lets change just the .js file.


	/* WebSocket - Communication using buffer */

	/* Create the WebSocket */

	//Server address.
	var wsUri = "ws://localhost:8081/WebSocket/imageEcho";
	var websocket = null;

	//Connect button.
	var connectBtn = document.getElementById("connectBtn");
	connectBtn.onclick = function() {
		if (websocket == null) {
			// Create WebSocket.
			websocket = new WebSocket(wsUri);
			// Define type of WebSocket, can be UTF-8, buffer or blob.
			websocket.binaryType = 'arraybuffer';

			// Add WebSocket default methods.
			websocket.onopen = function(evt) {
				onOpen(evt)
			};

			websocket.onmessage = function(evt) {
				onMessage(evt)
			};

			websocket.onerror = function(evt) {
				onError(evt)
			};
		}
	};

	function onOpen(evt) {
		writeToScreen("Connected to Endpoint!");
	}

	function onError(evt) {
		writeToScreen('ERROR: ' + evt.data);
	}

	function onMessage(evt) {
		// Our WebSocket only accepts communication throw binary (buffer).
		if(evt.data instanceof ArrayBuffer) {
			var message = bufferToStr(evt.data);
			writeToScreen(message);
		}
	}

	//Disconnect button.
	var disconnectBtn = document.getElementById("disconnectBtn");
	disconnectBtn.onclick = function() {
		if (websocket != null) {
			websocket.close();
			websocket = null;
			writeToScreen("Disconnected from Endpoint!");	
		}
	};

	var sendMessageBtn = document.getElementById("sendMessageBtn");
	sendMessageBtn.onclick = function() {
		if (websocket != null) {
			var message = document.getElementById("textID").value;
			var buffer = strToBuffer(message);
			websocket.send(buffer);
			writeToScreen(message);
		}
	};

	/* Screen information */

	function writeToScreen(message) {
		var pre = document.createElement("p");
		pre.style.wordWrap = "break-word";
		pre.innerHTML = message;
		output.appendChild(pre);
	}

	//Update UI.
	function init() {
		output = document.getElementById("output");
	}


	window.addEventListener("load", init, false);

	function strToBuffer(str) {
		var buffer = new ArrayBuffer(str.length * 2);
		var bytes = new Uint8Array(buffer);
		for (var i=0; i<bytes.length; i++) {
			bytes[i] = str.charCodeAt(i);
		}
		return buffer;
	}

	function bufferToStr(buf) {
		return String.fromCharCode.apply(null, new Uint8Array(buf));
	}


Produced by [Wiki Dreams.github.io](https://WikiDreams.github.io/).