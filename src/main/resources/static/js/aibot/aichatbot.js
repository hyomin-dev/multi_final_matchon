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

// === 챗봇 런처 및 모달 처리: 이벤트 위임 방식 적용 ===
document.addEventListener("DOMContentLoaded", () => {
    // 로그인 상태 확인 후 챗봇 제거
    fetch("/auth/check", { credentials: "include" })
        .then(res => {
            if (!res.ok) {
                const launcher = document.getElementById("chatbot-launcher");
                const modal = document.getElementById("chatbot-modal");
                if (launcher) launcher.remove();
                if (modal) modal.remove();
            }
        });

    // 이벤트 위임: 챗봇 열기/닫기
    document.addEventListener("click", function (e) {
        if (e.target.id === "chatbot-launcher") {
            fetch("/auth/check", { credentials: "include" })
                .then(res => {
                    if (res.ok) {
                        const modal = document.getElementById("chatbot-modal");
                        if (modal) modal.style.display = "flex";
                    } else {
                        alert("로그인 후 사용 가능합니다.");
                    }
                });
        }

        if (e.target.id === "chatbot-close") {
            const modal = document.getElementById("chatbot-modal");
            if (modal) modal.style.display = "none";
        }
    });
});

// === 챗봇 대화 UI 기능 ===
function initChat() {
    document.getElementById("chat-start").style.display = "none";
    document.getElementById("chat-ui").style.display = "block";
    appendBotMessage("안녕하세요! MatchON 챗봇입니다! 궁금하신 내용을 선택해 주세요!");
}

function appendMessage(text, isUser = false, bold = false) {
    const chatBox = document.getElementById("chat-box");
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

function escapeHtml(str) {
    return str.replace(/[&<>"']/g, match => ({
        '&': '&amp;',
        '<': '&lt;',
        '>': '&gt;',
        '"': '&quot;',
        "'": '&#39;',
    }[match]));
}

function appendBotMessage(text) {
    appendMessage(text, false);
}

function appendUserMessage(text, bold = false) {
    appendMessage(text, true, bold);
}

function sendMessage() {
    const input = document.getElementById("chat-input");
    const message = input.value.trim();
    if (!message) return;
    appendUserMessage(message);
    input.value = "";
    fetchBotReply(message);
}

function sendChip(text) {
    appendUserMessage(text, true);
    fetchBotReply(text);
}

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
