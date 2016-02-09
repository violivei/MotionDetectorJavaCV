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

