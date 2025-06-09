document.addEventListener("DOMContentLoaded", async () => {
    getMyTeamChatRooms();
});

async function getMyTeamChatRooms() {
    try {
        const response = await fetch("/chat/my/rooms/team", {
            method: "POST",
            credentials: "include",
            headers: { "Content-Type": "application/json" }
        });

        if (!response.ok) {
            const error = await response.json();
            console.error("❌ 팀 채팅방 로딩 실패:", error);
            throw new Error(`HTTP error! Status: ${response.status}`);
        }

        const result = await response.json();
        const rooms = result.data;

        const chatRoomList = document.querySelector(".chat-room-list");

        if (!rooms || rooms.length === 0) {
            chatRoomList.innerHTML = `
                <div class="no-result">
                    현재 참여 중인 팀 채팅방이 없습니다.
                </div>
            `;
            return;
        }

        rooms.forEach(room => {
            const card = document.createElement("div");
            card.className = "chat-card";

            card.innerHTML = `
                <div class="chat-col chat-name"><strong>${room.roomName}</strong></div>
                <div class="chat-col chat-group"><strong>팀 채팅</strong></div>
                <div class="chat-col chat-unread"><strong>${room.unreadCount || 0}</strong></div>
                <div class="chat-col chat-actions">
                    <button class="btn enter-btn">입장</button>
                </div>
            `;

            const enterBtn = card.querySelector(".enter-btn");
            enterBtn.addEventListener("click", () => {
                window.location.href = `/chat/my/room?roomId=${room.roomId}`;
                // or open in new tab:
                // window.open(`/chat/my/room?roomId=${room.roomId}`, "_blank");
            });

            chatRoomList.appendChild(card);
        });

    } catch (err) {
        console.error("🚨 오류 발생:", err);
        document.querySelector(".chat-room-list").innerHTML = `
            <div class="error-message">
                팀 채팅방을 불러오는 중 문제가 발생했습니다.
            </div>
        `;
    }
}