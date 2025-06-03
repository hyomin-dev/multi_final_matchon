document.addEventListener("DOMContentLoaded", function () {
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