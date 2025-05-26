document.addEventListener("DOMContentLoaded",async ()=>{
    const chatBox = document.getElementById('chat-box');
    const messageInput = document.getElementById('messageInput');
    const sendBtn = document.getElementById('sendBtn');

    const data = getJwtToken();
    console.log(data);

    // const roomId = new URLSearchParams(window.location.search).get('roomId');
    // const senderEmail = localStorage.getItem("email");
    // const token = localStorage.getItem("token");

    const sock = new SockJS(`http://localhost:8090/connect`);
    stompClient = webstomp.over(sock);

    stompClient.connect({ Authorization: `Bearer ${data}` }, () => {
        stompClient.subscribe(`/topic/1`, message => {
            const msg = JSON.parse(message.body);
            appendMessage(msg);
            scrollToBottom();
        }, { Authorization: `Bearer ${data}` });
    });
})

function getJwtToken(){
    const tokenCookie = document.cookie
        .split('; ')
        .find(cookie => cookie.startsWith('Authorization='));

    return tokenCookie ? tokenCookie.split('=')[1] : null;
}

let stompClient = null;

// // 1. 채팅 내역 불러오기
// fetch(`${YOUR_BACKEND_API_URL}/chat/history/${roomId}`)
//     .then(res => res.json())
//     .then(messages => {
//         messages.forEach(msg => appendMessage(msg));
//         scrollToBottom();
//     });

// 2. WebSocket 연결

/*const sock = new SockJS(`http://localhost:8090/connect`);
stompClient = webstomp.over(sock);

stompClient.connect({ Authorization: `Bearer ${token}` }, () => {
    stompClient.subscribe(`/topic/${roomId}`, message => {
        const msg = JSON.parse(message.body);
        appendMessage(msg);
        scrollToBottom();
    }, { Authorization: `Bearer ${token}` });
});*/

// // 3. 메시지 전송
// sendBtn.addEventListener('click', sendMessage);
// messageInput.addEventListener('keyup', (e) => {
//     if (e.key === 'Enter') sendMessage();
// });
//
// function sendMessage() {
//     const msgText = messageInput.value.trim();
//     if (!msgText) return;
//
//     const message = {
//         senderEmail,
//         message: msgText
//     };
//
//     stompClient.send(`/publish/${roomId}`, JSON.stringify(message), {});
//     messageInput.value = '';
// }

// function appendMessage(msg) {
//     const msgDiv = document.createElement('div');
//     msgDiv.className = 'chat-message ' + (msg.senderEmail === senderEmail ? 'sent' : 'received');
//     msgDiv.innerHTML = `<strong>${msg.senderEmail}:</strong> ${msg.message}`;
//     chatBox.appendChild(msgDiv);
// }
//
// function scrollToBottom() {
//     chatBox.scrollTop = chatBox.scrollHeight;
// }
//
// // 4. 나가기 전 disconnect
// window.addEventListener('beforeunload', () => {
//     fetch(`${YOUR_BACKEND_API_URL}/chat/room/${roomId}/read`, { method: "POST" });
//     if (stompClient && stompClient.connected) {
//         stompClient.unsubscribe(`/topic/${roomId}`);
//         stompClient.disconnect();
//     }
// });
