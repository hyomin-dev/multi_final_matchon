let stompClient = null;
let isSubscribed = false;
let count = 0;

let reconnectTimeout = null;

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

    initSideBar();

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

                    //openbtn.src = "/img/bell-ring-black.png";
                   //console.log(message);
                   const messageBody = JSON.parse(message.body);
                   console.log(messageBody);
                   console.log(messageBody.notificationMessage);

                    createNotiStructure(messageBody.notificationId, messageBody.notificationMessage, messageBody.createdDate);


                    //console.error("메시지 처리 중 에러", e);
                }, { Authorization: `Bearer ${token}` });
                isSubscribed = true;

                if(reconnectTimeout){
                    clearTimeout(reconnectTimeout);
                    reconnectTimeout = null;
                }

                resolve(); // 성공 시
            },
            (error) => {
                // console.error("STOMP 연결 실패");
                // //showErrorPage(error?.headers?.message || "Chat STOMP 연결 실패");
                // reject(error); // 실패 시
                onError(error);

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

function onError(error){
    console.warn("STOMP 연결 끊어짐. 재시도 중...",error);

    if(!reconnectTimeout){
        reconnectTimeout = setTimeout(()=>{
            connect();
        },reconnectDelay);
    }
}

async function initSideBar(){
    const openBtn = document.getElementById('openMiniDrawerBtn');
    const closeBtn = document.getElementById('closeMiniDrawerBtn');
    const miniDrawer = document.getElementById('miniDrawer');


    // 초기에 읽지 않은 메시지를 가져옴
    const notifications = await getUnreadNoti();

    setUnreadNoti(notifications);

    openBtn.onclick = () => {
        miniDrawer.style.display = 'block';
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
}

async function getUnreadNoti(){
    const response = await fetch("/notification/get/unread");
    if(!response.ok)
        throw new Error(`HTTP error! Status:${response.status}`)

    const data = await response.json();

    //console.log(data);
    return data.data;

}



function setUnreadNoti(notifications){
    console.log(notifications);

    if(notifications.length===0){
        const miniDrawer = document.getElementById('miniDrawer');

        const wrapper = document.createElement("div");
        const msg = document.createElement("span");

        wrapper.classList.add("notification");
        msg.textContent = "읽지 않은 알림이 없습니다.";

        wrapper.appendChild(msg);
        miniDrawer.appendChild(wrapper);
        return;
    }

    notifications.forEach(noti =>{

        createNotiStructure(noti.notificationId, noti.notificationMessage, noti.createdDate);


    });
}

function formatDate(dateStr) {
    const date = new Date(dateStr);
    const yyyy = date.getFullYear();
    const mm = String(date.getMonth() + 1).padStart(2, '0');
    const dd = String(date.getDate()).padStart(2, '0');
    const hh = String(date.getHours()).padStart(2, '0');
    const min = String(date.getMinutes()).padStart(2, '0');
    return `${yyyy}-${mm}-${dd} ${hh}:${min}`;
}

function createNotiStructure(notificationId, notificationMessage, createdDate) {

    const miniDrawer = document.getElementById('miniDrawer');
    const header = document.querySelector(".mini-drawer-header");

    const wrapper = document.createElement("div");
    const msg = document.createElement("span");


    wrapper.classList.add("notification");

    msg.textContent = notificationMessage;

    const dateSpan = document.createElement("span");
    dateSpan.classList.add("notification-date");
    dateSpan.textContent = formatDate(createdDate);

    wrapper.appendChild(msg);
    wrapper.appendChild(dateSpan);
    miniDrawer.appendChild(wrapper);
    miniDrawer.insertBefore(wrapper, header.nextSibling);


    msg.addEventListener("click",async ()=>{
        const response = await fetch(`/notification/update/unread?notificationId=${notificationId}`,{
            method: "GET",
            credentials: "include"
        });
        if(!response.ok)
            throw new Error(`HTTP error! Status:${response.status}`);

        const data = await response.json();

        wrapper.classList.add("clicked");
        msg.style.pointerEvents = "none"; // 클릭 자체를 비활성화
        msg.style.opacity = "0.6"; // 시각적으로도 회색 느낌

        if(data && typeof data.data === "string" && data.data.trim() !== ""){
            const reply = confirm("알림 페이지로 이동하시겠습니까?")
            if(reply)
                window.location.href = data.data;
        }else{
            alert("알림이 확인되었습니다.");
        }
    });
}





























