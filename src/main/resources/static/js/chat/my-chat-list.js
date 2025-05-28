document.addEventListener("DOMContentLoaded",async ()=>{
    getMyChatRooms();
})

async function getMyChatRooms(){
    const response = await fetch("/chat/my/rooms",{
        method: "POST",
        credentials: "include"
    });

    if(!response.ok){
        throw new Error(`HTTP error! Status:${response.status}`)
    }

    const chatRoomList = document.querySelector(".chat-room-list");

    const items = await response.json();
    console.log(items);

    items.data.forEach(item =>{
        const card = document.createElement("div");
        card.className = "chat-card";

        card.innerHTML = `
            <div class="chat-col chat-name"><strong>${item.roomName}</strong></div>
            <div class="chat-col chat-group"><strong>${expressIsGroutChat(item.isGroupChat)}</strong></div>
            <div class="chat-col chat-unread"><strong>${item.unReadCount}</strong></div>
            <div class="chat-col chat-actions">
                <button class="btn enter-btn">입장</button>
                <button class="btn exit-btn">퇴장</button>
            </div>           
        `;

        const enterBtn = card.querySelector(".enter-btn");
        const exitBtn = card.querySelector(".exit-btn");

        enterBtn.addEventListener("click",()=>{
            window.open(`/chat/my/room?roomId=${item.roomId}`,"_black");
        })


        chatRoomList.appendChild(card);


    });
}


function expressIsGroutChat(isGroupChat){
    if(isGroupChat === "true")
        return "그룹 채팅";
    else
        return "1대1 채팅";

}