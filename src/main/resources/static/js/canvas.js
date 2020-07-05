var stompClient = null;

// TODO: userListがboard.html側にあるのはよくないか。

function connect(regOpen) {
	var socket = new SockJS(contextPath +'tabulo-websocket');
	stompClient = Stomp.over(socket);
	stompClient.connect({}, function(frame) {
		console.log('Connected: ' + frame);
		stompClient.subscribe('/topic/board/' + boardId, function(greeting) {
			var content = JSON.parse(greeting.body);
			processDesc(content);
		});
		stompClient.subscribe('/topic/registration/' + boardId, function(registration) {
			var content = JSON.parse(registration.body);
			if(content.type == 'list'){
				list = content.list;
				array = list.split('\t');
				console.log(array);
				userList = array;
			}
			$('#userListNum').text(userList.length);
			$('#userListInner').empty();
			_.each(userList, function(user){
				$newUser = $('<div>' + user + "</div>");
				$('#userListInner').append($newUser);
			});

		});
		sendRegistration(regOpen);
	});
}

function disconnect(regClose) {
	sendRegistration(regClose);
	if (stompClient !== null) {
		stompClient.disconnect();
	}
	console.log("Disconnected");
}

function sendMessage(object) {
	if(!checkConnectionInner()){
		return;
	};
	stompClient.send("/app/process", {}, JSON.stringify(object));
}

function sendRegistration(object) {
	console.log(object);
	stompClient.send("/app/registration", {}, JSON.stringify(object));
}
