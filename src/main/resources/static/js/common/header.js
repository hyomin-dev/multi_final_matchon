let stompClient = null;
let isSubscribed = false;
let count = 0;
document.addEventListener("DOMContentLoaded", function () {
    const detailDto = document.querySelector("#header-detail-dto");
    const loginEmail = detailDto.dataset.loginEmail;

    /*
    * 아이콘 클릭 → 내 채팅방 목록 페이지로 이동
    * */
    const chatImg = document.querySelector("#chat-icon");

    if (chatImg) {
        chatImg.addEventListener("click", () => {
            window.location.href = "/chat/my/rooms"; // 새 창에서 열기
        });
    }

    fetch('/auth/check', {
        method: 'GET',
        credentials: 'include'
    }).then(res => res.json())
        .then(data => {
            const loginBtn = document.getElementById("btn-login");
            const signupBtn = document.getElementById("btn-signup");
            const myBtn = document.getElementById("btn-mypage");
            const adminBtn = document.getElementById("btn-admin");
            const logoutBtn = document.getElementById("btn-logout");

            if (data.role === "ADMIN") {
                // 관리자
                if (loginBtn) loginBtn.style.display = "none";
                if (signupBtn) signupBtn.style.display = "none";
                if (adminBtn) adminBtn.style.display = "inline-block";
                if (myBtn) myBtn.style.display = "none";
                if (logoutBtn) logoutBtn.style.display = "inline-block";
            } else {
                // 일반 유저
                if (loginBtn) loginBtn.style.display = "none";
                if (signupBtn) signupBtn.style.display = "none";
                if (adminBtn) adminBtn.style.display = "none";
                if (myBtn) myBtn.style.display = "inline-block";
                if (logoutBtn) logoutBtn.style.display = "inline-block";
            }
        })
        .catch(() => {
            // 로그인 안 되어 있을 경우
            document.getElementById("btn-login").style.display = "inline-block";
            document.getElementById("btn-signup").style.display = "inline-block";
            document.getElementById("btn-mypage").style.display = "none";
            document.getElementById("btn-admin").style.display = "none";
            document.getElementById("btn-logout").style.display = "none";
        });

    const openBtn = document.getElementById('openMiniDrawerBtn');
    const closeBtn = document.getElementById('closeMiniDrawerBtn');
    const miniDrawer = document.getElementById('miniDrawer');

    openBtn.onclick = () => {
        miniDrawer.style.display = 'block';
        const newEle = document.createElement("div");
        newEle.textContent = "여기에요.";
        miniDrawer.appendChild(newEle);

        // 예시 알림 리스트 (실제로는 fetch로 받아야 함)
        const notifications = [
            { id: 1, message: `📢 새로운 매칭 요청이 있습니다.${count++}` },
            { id: 2, message: `✅ 후기 작성을 완료해주세요.${count++}` },
            { id: 3, message: `📌 오늘 경기 일정이 있습니다.${count++}` }
        ];

        // 기존 알림 지우고 새로 렌더링
        const existing = miniDrawer.querySelectorAll(".notification");
        existing.forEach(el => el.remove());

        notifications.forEach(noti => {
            const wrapper = document.createElement("div");
            wrapper.className = "notification";

            const msg = document.createElement("span");
            msg.textContent = noti.message;

            const btn = document.createElement("button");
            btn.textContent = "읽음";
            btn.className = "read-btn";
            btn.onclick = () => {
                // // 서버에 읽음 처리 요청
                // fetch(`/api/notifications/${noti.id}/read`, {
                //     method: "PATCH",
                //     credentials: "include"
                // }).then(() => {
                //     wrapper.remove(); // 읽음 처리 후 UI에서 제거
                // });

                wrapper.remove(); // 읽음 처리 후 UI에서 제거
            };

            wrapper.appendChild(msg);
            wrapper.appendChild(btn);
            miniDrawer.appendChild(wrapper);
        });
    };

    closeBtn.onclick = () => {
        miniDrawer.style.display = 'none';
    };

    // 바깥 클릭 시 닫기 (선택사항)
    window.addEventListener("click", (e) => {
        if (!miniDrawer.contains(e.target) && e.target !== openBtn) {
            miniDrawer.style.display = 'none';
        }
    });

    initStomp(loginEmail);


});

// secureFetch: API 요청 → accessToken 만료 시 자동 재발급 → 다시 요청
function secureFetch(url, options = {}) {
    const method = options.method || 'GET';
    const headers = options.headers || {};
    const body = options.body || null;

    return fetch(url, {
        method: method,
        headers: headers,
        body: body,
        credentials: 'include'
    })
        .then(response => {
            if (response.status === 401) {
                // accessToken 만료 → refreshToken으로 재발급 시도
                return fetch('/auth/reissue', {
                    method: 'POST',
                    credentials: 'include'
                })
                    .then(reissueRes => {
                        if (reissueRes.ok) {
                            return fetch(url, {
                                method: method,
                                headers: headers,
                                body: body,
                                credentials: 'include'
                            });
                        } else {
                            alert("세션이 만료되었습니다. 다시 로그인해주세요.");
                            window.location.href = "/login";
                            throw new Error("세션 만료");
                        }
                    });
            }
            return response;
        });
}

// 로그아웃 처리 → accessToken, refreshToken 쿠키 삭제
function logout() {
    fetch('/auth/logout', {
        method: 'POST',
        credentials: 'include'
    })
        .then(res => {
            alert("로그아웃 되었습니다.");
            window.location.href = "/main?_=" + new Date().getTime();
        });
}

async function initStomp(loginEmail){

    //jwt token 획득
    const token = getJwtToken();

    // SockJs 설정
    setStompClient();

    //연결 해제 설정
    setDisconnects();

    // 연결 시도
    await connect(token, loginEmail);
    console.log("STOMP 연결 성공");


}

function getJwtToken(){
    const tokenCookie = document.cookie
        .split('; ')
        .find(cookie => cookie.startsWith('Authorization='));

    return tokenCookie ? tokenCookie.split('=')[1] : null;
}

function setStompClient() {
    const sock = new SockJS(`/connect`);
    stompClient = webstomp.over(sock, {debug:false});
}

function connect(token, loginEmail) {
    const openbtn = document.querySelector("#openMiniDrawerBtn");

    if(stompClient && stompClient.connected)
        return; // 중복 연결 방지

    return new Promise((resolve, reject) => {
        stompClient.connect(
            { Authorization: `Bearer ${token}` },
            () => {
                // ✅ 연결 성공
                if(isSubscribed&&stompClient.connected)
                    return;
                console.log("STOMP 연결 성공");
                stompClient.subscribe(`/user/${loginEmail}/notify`, message => {
                    //const body = JSON.parse(message.body);
                    openbtn.src = "/img/bell-ring-black.png";
                   console.log(message);
                   console.log(message.body);

                    //console.error("메시지 처리 중 에러", e);
                }, { Authorization: `Bearer ${token}` });
                isSubscribed = true;
                resolve(); // 성공 시
            },
            (error) => {
                console.error("STOMP 연결 실패");
                //showErrorPage(error?.headers?.message || "Chat STOMP 연결 실패");
                reject(error); // 실패 시
            }
        );
    });
}


function setDisconnects(roomId) {
// 4. 나가기 전 disconnect
    window.addEventListener('beforeunload', () => {
        // fetch(`/chat/room/${roomId}/read`, { method: "POST" });
        // if (stompClient && stompClient.connected) {
        //     stompClient.unsubscribe(`/topic/${roomId}`);
        //     stompClient.disconnect();
        // }
        // const data = new Blob([JSON.stringify({ roomId })], { type: 'application/json' });
        // navigator.sendBeacon(`/chat/room/read?roomId=${roomId}`, data);

        if (stompClient && stompClient.connected) {
            stompClient.unsubscribe(`/notify`);
            stompClient.disconnect(); // 이건 백그라운드 전송 못함 → 실패할 수 있음
        }

    });

    document.addEventListener('visibilitychange', () => {
        if (document.visibilityState === 'hidden') {
            //fetch(`/chat/room/read?roomId=${roomId}`, { method: "POST" });
            if (stompClient && stompClient.connected) {
                stompClient.unsubscribe(`/notify`);
                stompClient.disconnect();
            }
        }
    });

    window.addEventListener('pagehide', () => {
        //fetch(`/chat/room/read?roomId=${roomId}`, { method: "POST" });
        if (stompClient && stompClient.connected) {
            stompClient.unsubscribe(`/notify`);
            stompClient.disconnect();
        }
    });
}




























