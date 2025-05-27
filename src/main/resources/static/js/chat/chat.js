let stompClient = null;
let chatBox = document.getElementById('chat-box');
let messageInput = document.getElementById('messageInput');
let sendBtn = document.getElementById('sendBtn');
let loginEmail = "";
document.addEventListener("DOMContentLoaded",async ()=>{

    const detailDto = document.querySelector("#chat1-1-detail-dto");
    // if(!detailDto)
    //     return;
    loginEmail = detailDto.dataset.loginEmail;
    const data = getJwtToken();
    console.log(data);

    // const roomId = new URLSearchParams(window.location.search).get('roomId');
    // const senderEmail = localStorage.getItem("email");
    // const token = localStorage.getItem("token");

    const sock = new SockJS(`http://localhost:8090/connect`);
    stompClient = webstomp.over(sock);

    stompClient.connect({ Authorization: `Bearer ${data}` }, () => {
        stompClient.subscribe(`/topic/1`, message => {
            console.log(message);
            const msg = JSON.parse(message.body);
            appendMessage(msg);
            scrollToBottom();
        }, {}); //Authorization: `Bearer ${data}`
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
        if (e.key === 'Enter') sendMessage();
    });

    // 4. 나가기 전 disconnect
    window.addEventListener('beforeunload', () => {
        fetch(`http://localhost:8090/chat/room/1/read`, { method: "POST" });
        if (stompClient && stompClient.connected) {
            stompClient.unsubscribe(`/topic/1`);
            stompClient.disconnect();
        }
    });


})

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

function sendMessage() {
    const msgText = messageInput.value.trim();
    if (!msgText) return;

    const content = {
        content: msgText
    };

    stompClient.send(`/publish/1`, JSON.stringify(content), {});
    messageInput.value = '';
}

function appendMessage(msg) {
    const msgDiv = document.createElement('div');
    msgDiv.className = 'chat-message ' + (msg.senderEmail === loginEmail ? 'sent' : 'received');
    msgDiv.innerHTML = `<strong>${msg.senderEmail}:</strong> ${msg.content}`;
    chatBox.appendChild(msgDiv);
}

function scrollToBottom() {
    chatBox.scrollTop = chatBox.scrollHeight;
}


