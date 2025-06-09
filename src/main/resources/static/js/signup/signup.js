document.getElementById("signupForm").addEventListener("submit", async function(e) {
    e.preventDefault();

    const role = document.getElementById("roleSelect").value;
    const url = role === "host" ? "/auth/signup/host" : "/auth/signup/user";

    const email = document.getElementById("email").value;
    const password = document.getElementById("password").value;
    const name = document.getElementById("name").value;
    const emailAgreement = document.getElementById("emailAgreement").checked;

    // 비밀번호 유효성 검사
    const passwordError = getPasswordValidationMessage(password);
    if (passwordError) {
        const msgEl = document.getElementById("passwordCheckMsg");
        msgEl.textContent = passwordError;
        msgEl.className = "message error";
        return;
    }

    // 이메일 중복 검사
    const emailCheck = await fetch(`/auth/check-email?email=${encodeURIComponent(email)}`);
    const emailResult = await emailCheck.json();
    if (emailResult.exists) {
        alert("이미 사용 중인 이메일입니다.");
        return;
    }

    // payload 구성
    const payload = { email, password, name, emailAgreement };
    if (role === "user") {
        payload.positionId = null;
    }

    // 회원가입 요청
    fetch(url, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(payload)
    })
        .then(res => {
            if (res.ok) {
                alert("회원가입 성공!");
                window.location.href = "/login";
            } else {
                return res.json().then(data => {
                    alert(data.error || "회원가입 실패");
                });
            }
        });
});


document.getElementById("emailCheckBtn").addEventListener("click", async function () {
    const email = document.getElementById("email").value;
    const msgEl = document.getElementById("emailCheckMsg");

    if (!email) {
        msgEl.textContent = "이메일을 입력해 주세요.";
        msgEl.className = "message error";
        return;
    }

    try {
        const res = await fetch(`/auth/check-email?email=${encodeURIComponent(email)}`);
        if (!res.ok) throw new Error("서버 오류");

        const data = await res.json();

        if (data.exists) {
            msgEl.textContent = "이미 사용 중인 이메일입니다.";
            msgEl.className = "message error";
        } else {
            msgEl.textContent = "사용 가능한 이메일입니다.";
            msgEl.className = "message success";
        }
    } catch (err) {
        msgEl.textContent = "이메일 확인 중 오류가 발생했습니다.";
        msgEl.className = "message error";
        console.error("이메일 확인 오류:", err);
    }
});

document.getElementById("passwordCheckBtn").addEventListener("click", function () {
    const password = document.getElementById("password").value;
    const msgEl = document.getElementById("passwordCheckMsg");

    const errorMessage = getPasswordValidationMessage(password);
    if (errorMessage) {
        msgEl.textContent = errorMessage;
        msgEl.className = "message error";
    } else {
        msgEl.textContent = "사용 가능한 비밀번호입니다.";
        msgEl.className = "message success";
    }
});

// 비밀번호 조건 함수
function getPasswordValidationMessage(password) {
    if (!/^.{8,}$/.test(password)) return "비밀번호는 8자 이상이어야 합니다.";
    if (!/[A-Z]/.test(password)) return "비밀번호에 대문자를 포함해야 합니다.";
    if (!/[a-z]/.test(password)) return "비밀번호에 소문자를 포함해야 합니다.";
    if (!/[0-9]/.test(password)) return "비밀번호에 숫자를 포함해야 합니다.";
    if (!/[!@#$%^&*(),.?\":{}|<>]/.test(password)) return "비밀번호에 특수문자를 포함해야 합니다.";
    if (/(.)\1\1/.test(password)) return "같은 문자를 연속으로 사용할 수 없습니다.";
    return null;
}
