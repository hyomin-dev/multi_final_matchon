let stompClient = null;
let chatBox = document.getElementById('chat-box');
let messageInput = document.getElementById('messageInput');
let sendBtn = document.getElementById('sendBtn');




document.addEventListener("DOMContentLoaded",async ()=>{
    let roomId = "";
    const detailDto = document.querySelector("#chat1-1-detail-dto");
    // if(!detailDto)
    //     return;
    const loginEmail = detailDto.dataset.loginEmail;
    const receiverId = Number(detailDto.dataset.receiverId);
    roomId = Number(detailDto.dataset.roomId);


    const token = getJwtToken();
    //console.log(data);

    // const roomId = new URLSearchParams(window.location.search).get('roomId');
    // const senderEmail = localStorage.getItem("email");
    // const token = localStorage.getItem("token");

    if(!roomId && receiverId)
        roomId = Number(await getPrivateChatRoomId(receiverId));



    //console.log(roomId);
    //roomId = 100;
    await connect(token, roomId, loginEmail);
    await getChatHistory(roomId, loginEmail);



    // 3. 메시지 전송
    sendBtn.addEventListener('click', ()=>{
        sendMessage(roomId);
    });
    messageInput.addEventListener('keyup', (e) => {
        if (e.key === 'Enter') sendMessage(roomId);
    });

    // 4. 나가기 전 disconnect
    window.addEventListener('beforeunload', () => {
        // fetch(`/chat/room/${roomId}/read`, { method: "POST" });
        // if (stompClient && stompClient.connected) {
        //     stompClient.unsubscribe(`/topic/${roomId}`);
        //     stompClient.disconnect();
        // }
        const data = new Blob([JSON.stringify({ roomId })], { type: 'application/json' });
        navigator.sendBeacon(`/chat/room/read?roomId=${roomId}`, data);

        if (stompClient && stompClient.connected) {
            stompClient.disconnect(); // 이건 백그라운드 전송 못함 → 실패할 수 있음
        }

    });

    document.addEventListener('visibilitychange', () => {
        if (document.visibilityState === 'hidden') {
            fetch(`/chat/room/read?roomId=${roomId}`, { method: "POST" });
            if (stompClient && stompClient.connected) {
                stompClient.unsubscribe(`/topic/${roomId}`);
                stompClient.disconnect();
            }
        }
    });

    window.addEventListener('pagehide', () => {
        fetch(`/chat/room/read?roomId=${roomId}`, { method: "POST" });
        if (stompClient && stompClient.connected) {
            stompClient.unsubscribe(`/topic/${roomId}`);
            stompClient.disconnect();
        }
    });
})

async function getPrivateChatRoomId(receiverId){
    const response = await fetch(`/chat/room/private?receiverId=${receiverId}`,{
        method: "GET",
        credentials: "include"
    });
    if(!response.ok){
       const data = await response.json();
       // console.log(data);
       // console.log(data.error);
       const form  = document.createElement("form");
       form.method = "POST";
       form.action = "/error/chat";

       const input = document.createElement("input");
       input.type = "hidden";
       input.name = "error";
       input.value = data.error;
       form.appendChild(input);
       document.body.appendChild(form);
       form.submit();
    }

    const data = await response.json();

    return data.data;

}

async function getChatHistory(roomId, loginEmail) {
    const response = await fetch(`/chat/history?roomId=${roomId}`,{
        method: "GET",
        credentials: "include"
    });
    if(!response.ok){
        const data = await response.json();
        // console.log(data);
        // console.log(data.error);
        const form  = document.createElement("form");
        form.method = "POST";
        form.action = "/error/chat";

        const input = document.createElement("input");
        input.type = "hidden";
        input.name = "error";
        input.value = data.error;
        form.appendChild(input);
        document.body.appendChild(form);
        form.submit();
    }

    const data = await response.json();

    data.data.forEach(dto =>{
        appendMessage(dto, loginEmail);
        scrollToBottom();
    })

}


function getJwtToken(){
    const tokenCookie = document.cookie
        .split('; ')
        .find(cookie => cookie.startsWith('Authorization='));

    return tokenCookie ? tokenCookie.split('=')[1] : null;
}


// // 1. 채팅 내역 불러오기
// fetch(`${YOUR_BACKEND_API_URL}/chat/history/${roomId}`)
//     .then(res => res.json())
//     .then(messages => {
//         messages.forEach(msg => appendMessage(msg));
//         scrollToBottom();
//     });


function connect(token, roomId,loginEmail) {
    return new Promise((resolve, reject) => {
        const sock = new SockJS(`/connect`);
        stompClient = webstomp.over(sock);

        stompClient.connect(
            { Authorization: `Bearer ${token}` },
            () => {
                stompClient.subscribe(`/topic/${roomId}`, message => {
                    const msg = JSON.parse(message.body);
                    appendMessage(msg, loginEmail);
                    scrollToBottom();
                }, { Authorization: `Bearer ${token}` });

                resolve(); // ✅ 연결 성공 시점에 resolve 호출
            },
            (error) => {
                // console.error("STOMP 연결 실패:", error);
                // reject(error); // ❗연결 실패 시 reject 호출

                const form  = document.createElement("form");
                form.method = "POST";
                form.action = "/error/chat";

                const input = document.createElement("input");
                input.type = "hidden";
                input.name = "error";
                input.value = "Chat 연결 실패: 서버에 접속할 수 없습니다."
                form.appendChild(input);
                document.body.appendChild(form);
                form.submit();
            }
        );
    });
}





function sendMessage(roomId) {
    const msgText = messageInput.value.trim();
    if (!msgText) return;

    const content = {
        content: msgText
    };

    stompClient.send(`/publish/${roomId}`, JSON.stringify(content));
    messageInput.value = '';
}

function appendMessage(msg, loginEmail) {
    const msgDiv = document.createElement('div');
    msgDiv.className = 'chat-message ' + (msg.senderEmail === loginEmail ? 'sent' : 'received');
    msgDiv.innerHTML = `<strong>${msg.senderName}:</strong> ${msg.content}`;
    chatBox.appendChild(msgDiv);
}

function scrollToBottom() {
    chatBox.scrollTop = chatBox.scrollHeight;
}


