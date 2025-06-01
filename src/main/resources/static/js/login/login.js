document.getElementById("loginForm").addEventListener("submit", function (e) {
    e.preventDefault();

    fetch("/auth/login", {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        credentials: 'include',
        body: JSON.stringify({
            email: document.getElementById("email").value,
            password: document.getElementById("password").value
        })
    })
        .then(res => {
            if (!res.ok) {
                return res.json().then(data => {
                    // ✅ [수정된 부분 시작]
                    // 정지된 계정인 경우 → login 페이지로 리다이렉트
                    if (data.error === "suspended") {
                        const date = encodeURIComponent(data.date || "");
                        window.location.href = `/login?error=suspended&date=${date}`;
                    } else {
                        throw new Error(data.error || "로그인 실패");
                    }
                    // ✅ [수정된 부분 끝]
                });
            }
            return res.json();
        })
        .then(data => {
            // 로그인 성공 후 사용자 권한 확인
            fetch("/auth/check", {
                method: "GET",
                credentials: "include"
            })
                .then(res => res.json())
                .then(user => {
                    if (user.role === "ADMIN") {
                        alert("관리자로 로그인되었습니다.");
                        window.location.href = "/main";
                    } else {
                        alert("로그인 성공!");
                        window.location.href = "/main";
                    }
                });
        })
        .catch(err => {
            // 일반 로그인 오류
            alert("에러: " + err.message);
        });
});
