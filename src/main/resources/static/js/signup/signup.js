document.getElementById("signupForm").addEventListener("submit", function(e) {
    e.preventDefault();

    const role = document.getElementById("roleSelect").value;
    const url = role === "host" ? "/auth/signup/host" : "/auth/signup/user";

    const payload = {
        email: document.getElementById("email").value,
        password: document.getElementById("password").value,
        name: document.getElementById("name").value,
    };

    if (role === "user") {
        payload.positionId = null;
    }

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