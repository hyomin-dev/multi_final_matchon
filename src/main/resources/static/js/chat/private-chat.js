let stompClient = null;
let chatBox = document.getElementById('chat-box');
let messageInput = document.getElementById('messageInput');
let sendBtn = document.getElementById('sendBtn');

let roomId = "";

document.addEventListener("DOMContentLoaded",async ()=>{

    const detailDto = document.querySelector("#chat1-1-detail-dto");
    // if(!detailDto)
    //     return;
    const loginEmail = detailDto.dataset.loginEmail;
    const receiverId = Number(detailDto.dataset.receiverId);
    roomId = Number(detailDto.dataset.roomId);

    const data = getJwtToken();
    //console.log(data);

    // const roomId = new URLSearchParams(window.location.search).get('roomId');
    // const senderEmail = localStorage.getItem("email");
    // const token = localStorage.getItem("token");

    if(!roomId && receiverId)
        roomId = await getPrivateChatRoomId(receiverId);


    const sock = new SockJS(`/connect`);
    stompClient = webstomp.over(sock);

    stompClient.connect({ Authorization: `Bearer ${data}` }, () => {
        stompClient.subscribe(`/topic/${roomId}`, message => {
            console.log(message);
            const msg = JSON.parse(message.body);
            appendMessage(msg, loginEmail);
            scrollToBottom();
        }, {Authorization: `Bearer ${data}`}); //
    });

    /*stompClient.connect({}, () => {
        stompClient.subscribe(`/topic/1`, message => {
            const msg = JSON.parse(message.body);
            appendMessage(msg);
            scrollToBottom();
        }, { });
    });*/

    // 3. 메시지 전송
    sendBtn.addEventListener('click', sendMessage);
    messageInput.addEventListener('keyup', (e) => {
        if (e.key === 'Enter') sendMessage(roomId);
    });

    // 4. 나가기 전 disconnect
    window.addEventListener('beforeunload', () => {
        fetch(`/chat/room/${roomId}/read`, { method: "POST" });
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
       console.log(data);
       console.log(data.error);
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

function sendMessage(roomId) {
    const msgText = messageInput.value.trim();
    if (!msgText) return;

    const content = {
        content: msgText
    };

    stompClient.send(`/publish/${roomId}`, JSON.stringify(content), {});
    messageInput.value = '';
}

function appendMessage(msg, loginEmail) {
    const msgDiv = document.createElement('div');
    msgDiv.className = 'chat-message ' + (msg.senderEmail === loginEmail ? 'sent' : 'received');
    msgDiv.innerHTML = `<strong>${msg.senderEmail}:</strong> ${msg.content}`;
    chatBox.appendChild(msgDiv);
}

function scrollToBottom() {
    chatBox.scrollTop = chatBox.scrollHeight;
}


