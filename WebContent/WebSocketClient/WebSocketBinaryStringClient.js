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
