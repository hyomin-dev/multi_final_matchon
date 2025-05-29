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
                <button class="btn enter-btn"></button>
                <button class="btn exit-btn"></button>
            </div>           
        `;

        const enterBtn = card.querySelector(".enter-btn");
        enterBtn.textContent = "입장";


        const exitBtn = card.querySelector(".exit-btn");
        if(item.isGroupChat === true){

            exitBtn.textContent = "나가기";
        }

        else{

            if(item.isBlock===true){
                exitBtn.textContent = "차단해제";
                exitBtn.addEventListener("click",(e)=>{
                    let reply = confirm("정말 차단 해제 하시겠습니까?");
                    if(reply){
                        window.location.href = `/chat/room/private/unblock?roomId=${item.roomId}`;
                    }else{
                        e.preventDefault();
                    }
                });

               enterBtn.disabled=true;


            }else{
                exitBtn.textContent = "차단";
                exitBtn.addEventListener("click",(e)=>{
                    let reply = confirm("정말 차단 하시겠습니까?");
                    if(reply){
                        window.location.href = `/chat/room/private/block?roomId=${item.roomId}`;
                    }else{
                        e.preventDefault();
                    }
                });

                enterBtn.addEventListener("click",()=>{
                    window.open(`/chat/my/room?roomId=${item.roomId}`,"_black");
                });

            }

        }

        // enterBtn.addEventListener("click",()=>{
        //     window.open(`/chat/my/room?roomId=${item.roomId}`,"_black");
        // });
        //
        // exitBtn.addEventListener("click",(e)=>{
        //     let reply = confirm("정말 차단하시겠습니까?");
        //     if(reply){
        //         window.location.href = `/chat/room/private/block?roomId=${item.roomId}`;
        //     }else{
        //         e.preventDefault();
        //     }
        // })

        chatRoomList.appendChild(card);

    });
}


function expressIsGroutChat(isGroupChat){
    if(isGroupChat === "true")
        return "그룹 채팅";
    else
        return "1대1 채팅";

}
