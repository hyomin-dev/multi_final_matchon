// 로그인 처리
document.getElementById("loginForm").addEventListener("submit", function (e) {
    e.preventDefault();

    const email = document.getElementById("email").value;
    const password = document.getElementById("password").value;

    let redirect = localStorage.getItem("loginRedirectUrl");
    if (!redirect || redirect === "null" || redirect === "undefined" || redirect === "/signup") {
        redirect = "/main";
    }

    fetch("/auth/login", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        credentials: 'include',
        body: JSON.stringify({ email, password })
    })
        .then(res => {
            if (!res.ok) {
                return res.json().then(data => {
                    if (data.error === "존재하지 않는 사용자입니다." || data.error === "탈퇴한 계정입니다.") {
                        //alert("존재하지 않는 계정입니다. 회원가입 후 이용해주세요.");
                        Swal.fire({text: '존재하지 않는 계정입니다. 회원가입 후 이용해주세요.', icon: 'warning', confirmButtonText: '확인'}).then(()=>{
                            window.location.href = "/signup";
                        });
                    } else {
                        throw new Error(data.error || "로그인 실패");
                    }
                });
            }
            return res.json();
        })
        .then(data => {
            if (data.isTemporaryPassword) {
                localStorage.setItem("showPasswordPopup", "true");
            } else {
                localStorage.removeItem("showPasswordPopup");
            }

            //alert("로그인 성공!");
            Swal.fire({text: '로그인 성공!', icon: 'success', confirmButtonText: '확인'}).then(()=>{
                window.location.href = "/main";
            });
        })
});

// 로그인 페이지 진입 시 팝업 조건 확인
window.addEventListener("DOMContentLoaded", function () {
    const shouldShowPopup = localStorage.getItem("showPasswordPopup") === "true";

    if (shouldShowPopup) {
        fetch("/auth/check", {
            method: "GET",
            credentials: "include"
        })
            .then(res => {
                if (!res.ok) throw new Error("인증 실패");
                return res.json();
            })
            .then(user => {
                if (user && user.isTemporaryPassword) {
                    openChangePasswordPopup();
                }
                localStorage.removeItem("showPasswordPopup");
            })
            .catch(() => {
                // 인증 실패 시 팝업 제거
                localStorage.removeItem("showPasswordPopup");
                closeResetPopup();
                closeChangePopup();
            });
    } else {
        closeResetPopup();
        closeChangePopup();
    }
});

// "비밀번호를 잊으셨습니까?" 팝업 열기
document.getElementById("openResetPopup").addEventListener("click", function (e) {
    e.preventDefault();
    openResetPopup();
});

function openResetPopup() {
    document.getElementById("resetPopup").style.display = "block";
    document.getElementById("popupOverlay").style.display = "block";
}

function closeResetPopup() {
    document.getElementById("resetPopup").style.display = "none";
    document.getElementById("popupOverlay").style.display = "none";
    document.getElementById("resetResult").innerText = "";
}

// 임시 비밀번호 발급
document.getElementById("resetForm").addEventListener("submit", function (e) {
    e.preventDefault();
    const email = document.getElementById("resetEmail").value;

    fetch("/auth/reset-password", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ email })
    })
        .then(res => res.text())
        .then(msg => {
            document.getElementById("resetResult").innerText = msg;
            document.getElementById("resetResult").style.color = "#28a745";
        })
        .catch(err => {
            //alert("에러: " + err.message);
            Swal.fire({text: "에러: " + err.message, icon: 'warning', confirmButtonText: '확인'});
        });
});

function openChangePasswordPopup() {
    document.getElementById("changePasswordPopup").style.display = "block";
    document.getElementById("popupOverlay").style.display = "block";
}

function closeChangePopup() {
    document.getElementById("changePasswordPopup").style.display = "none";
    document.getElementById("popupOverlay").style.display = "none";
    document.getElementById("changeResult").innerText = "";
}

document.getElementById("changePasswordForm").addEventListener("submit", function (e) {
    e.preventDefault();

    const newPassword = document.getElementById("newPassword").value;
    const confirmPassword = document.getElementById("confirmPassword").value;

    fetch("/auth/change-password", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        credentials: "include",
        body: JSON.stringify({ newPassword, confirmPassword })
    })
        .then(res => {
            if (!res.ok) {
                return res.text().then(msg => {
                    console.error("서버 응답 에러 메시지:", msg);
                    document.getElementById("changeResult").innerText = msg;
                    document.getElementById("changeResult").style.color = "red";
                    throw new Error(msg);
                });
            }
            return res.text();
        })
        .then(msg => {
            document.getElementById("changeResult").innerText = msg;
            document.getElementById("changeResult").style.color = "#28a745";
            setTimeout(() => {
                closeChangePopup();
                window.location.href = "/main";
            }, 1500);
        })
        .catch(err => {
            console.warn("비밀번호 변경 실패 (catch):", err.message);
            const resultEl = document.getElementById("changeResult");
            resultEl.innerText = err.message || "비밀번호 변경 실패";
            resultEl.style.color = "red";
        });
});


