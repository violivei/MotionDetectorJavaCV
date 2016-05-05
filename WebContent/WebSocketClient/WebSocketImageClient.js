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
var wsUri = "ws://localhost:8080/WebSocket/imageEcho";
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
