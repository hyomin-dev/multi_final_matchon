// 날짜 및 입력창 엔터 처리
window.onload = () => {
    const today = new Date();
    const dateString = today.toLocaleDateString("ko-KR", {
        year: 'numeric', month: 'long', day: 'numeric', weekday: 'long'
    });
    const dateEl = document.getElementById("date");
    if (dateEl) dateEl.innerText = dateString;

    const input = document.getElementById("chat-input");
    if (input) {
        input.addEventListener("keypress", function (e) {
            if (e.key === "Enter") sendMessage();
        });
    }


};


// 챗봇 런처 및 모달 처리 (이벤트 위임 방식)
document.addEventListener("DOMContentLoaded", () => {
    // 로그인 확인 → 로그인 안 됐으면 챗봇 요소 제거

    fetch("/auth/check", { credentials: "include" })
        .then(res => {
            if (!res.ok) {
                if (launcher) launcher.remove();
                if (modal) modal.remove();
            }
        });


   
    document.addEventListener("click", function (e) {
        if (e.target.id === "chatbot-launcher") {
            fetch("/auth/check", { credentials: "include" })
                .then(res => {
                    if (res.ok) {
                        modal.style.display = "flex";
                    } else {
                        alert("로그인 후 사용 가능합니다.");
                    }
                });
        });
    }

    if (closeBtn) {
        closeBtn.addEventListener("click", () => {
            if (modal) modal.style.display = "none";
        });
    }
});


// 챗봇 대화 시작

function initChat() {
    const start = document.getElementById("chat-start");
    const ui = document.getElementById("chat-ui");
    if (start) start.style.display = "none";
    if (ui) ui.style.display = "block";
    appendBotMessage("안녕하세요! MatchON 챗봇입니다! 궁금하신 내용을 선택해 주세요!");
}

// 메시지 출력
function appendMessage(text, isUser = false, bold = false) {
    const chatBox = document.getElementById("chat-box");
    if (!chatBox) return;

    const wrapper = document.createElement("div");
    const content = document.createElement("div");
    const time = document.createElement("div");

    wrapper.className = `message ${isUser ? 'user' : 'bot'}`;
    content.className = "msg-content";
    time.className = "timestamp";
    time.textContent = new Date().toTimeString().substring(0, 5);

    const formattedText = bold ? `<b>${escapeHtml(text)}</b>` : escapeHtml(text);
    content.innerHTML = formattedText;

    if (!isUser) {
        const botIcon = document.createElement("img");
        botIcon.src = "/img/aichatbot.png";
        botIcon.className = "bot-icon";
        wrapper.appendChild(botIcon);
    }

    wrapper.appendChild(content);
    wrapper.appendChild(time);

    setTimeout(() => {
        chatBox.appendChild(wrapper);
        chatBox.scrollTop = chatBox.scrollHeight;
    }, isUser ? 0 : 100);
}

// HTML 이스케이프
function escapeHtml(str) {
    return str.replace(/[&<>"']/g, match => ({
        '&': '&amp;',
        '<': '&lt;',
        '>': '&gt;',
        '"': '&quot;',
        "'": '&#39;',
    }[match]));
}

// 봇/유저 메시지
function appendBotMessage(text) {
    appendMessage(text, false);
}

function appendUserMessage(text, bold = false) {
    appendMessage(text, true, bold);
}

// 전송 버튼 클릭 or 엔터
function sendMessage() {
    const input = document.getElementById("chat-input");
    const message = input.value.trim();
    if (!message) return;
    appendUserMessage(message);
    input.value = "";
    fetchBotReply(message);
}

// 추천 버튼 클릭 시
function sendChip(text) {
    appendUserMessage(text, true);
    fetchBotReply(text);
}

// 서버에 질문 전송 → 답변 수신
function fetchBotReply(message) {
    fetch("/api/aichat", {
        method: "POST",
        headers: { 'Content-Type': 'application/json' },
        credentials: "include",
        body: JSON.stringify({ message })
    })
        .then(res => res.json())
        .then(data => {
            const reply = data.reply;
            if (message.includes("종료")) {
                appendBotMessage("상담을 종료합니다.");
            } else {
                appendBotMessage(reply);
            }
        });
}
