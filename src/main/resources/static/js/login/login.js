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
                    throw new Error(data.error || "로그인 실패");
                });
            }
            return res.json();
        })
        .then(data => {
            // 쿠키에 토큰 자동 저장됨 → localStorage에 저장할 필요 없음
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
            alert("에러: " + err.message);
        });
});