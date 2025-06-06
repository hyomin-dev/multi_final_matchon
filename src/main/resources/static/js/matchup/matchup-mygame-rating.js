
let boardId = '';
document.addEventListener("DOMContentLoaded",()=>{
    const detailDto = document.querySelector("#matchup-rating-detail-dto");
    boardId = Number(detailDto.dataset.boardId);

    loadItems(1) // 프론트는 페이지 번호 시작을 1부터, 헷갈림
})

async function loadItems(page){
    const response = await fetch(`/matchup/rating/list?page=${page-1}&boardId=${boardId}`,{

        method: "GET",
        credentials: "include"
    });
    if(!response.ok)
        throw new Error(`HTTP error! Status:${response.status}`)
    const data = await response.json();
    //console.log(data);
    const items = data.data.items;
    const pageInfo = data.data.pageInfo;
    //console.log(pageInfo);

    renderList(items);
    renderPagination(pageInfo);


}
function renderList(items){


    const matchArea = document.querySelector("#match-container");
    matchArea.innerHTML = '';

    if(items.length ===0){
        matchArea.innerHTML = `
            <div class="no-result">
                매너 온도 평가할 대상이 없습니다.
            </div>
        `;
        return;
    }

    items.forEach((item, index)=>{

        const card = document.createElement("tr");
        card.className = "matchup-card";
        card.innerHTML = `
                    <td>${item.targetName}</td>
                    <td>${setReceivedScore(item)}</td>
                    <td class="truncate">${setReceivedReview(item)}</td>
                    <td>${setSendedScore(item)}</td>
                    <td class="truncate">${setSendedReview(item)}</td>       
                    <td><button class="send-btn">후기 작성</button></td>                                     
                `;
        setSendBtn(card,item);
        matchArea.appendChild(card);

    })
}

function renderPagination(pageInfo){

    // 프론트는 페이지 시작번호 1부터로 헷갈림
    const pageBlockSize = 5;
    // 프론트 측 page 시작 번호 1부터 변경
    const curPage = pageInfo.page + 1;


    const pagingArea = document.querySelector("#paging-container");
    pagingArea.innerHTML = '';


    const currentBlock = Math.floor((curPage-1)/pageBlockSize);
    const startPage = currentBlock * pageBlockSize+1;
    const endPage = Math.min(startPage + pageBlockSize-1,pageInfo.totalPages);

    // 첫 번째 블록으로 이동
    if (startPage > 1){
        const firstBtn = document.createElement("button");
        firstBtn.textContent = "<<";
        firstBtn.addEventListener("click",()=>{
            loadItems(1);

        });
        pagingArea.appendChild(firstBtn);
    }

    // 이전 블록으로 이동
    if (startPage > 1){
        const prevBtn = document.createElement("button");
        prevBtn.textContent = "<";
        prevBtn.addEventListener("click",()=>{
            loadItems(startPage-1);

        });
        pagingArea.appendChild(prevBtn);
    }

    // 현재 블록의 페이지 번호 버튼들
    for(let i=startPage; i<=endPage;i++){
        const btn = document.createElement("button");
        btn.textContent = i;
        if( i=== curPage)
            //btn.disabled = true;
            btn.classList.add("active");

        btn.addEventListener("click",()=>{
            loadItems(i);
        })
        pagingArea.appendChild(btn);
    }

    // 다음 블록으로 이동
    if(endPage < pageInfo.totalPages){
        const nextBtn = document.createElement("button");
        nextBtn.textContent = ">";
        nextBtn.addEventListener("click",()=>{
            loadItems(endPage+1);

        })
        pagingArea.appendChild(nextBtn);
    }

    // 마지막 블록으로 이동

    if(endPage<pageInfo.totalPages){
        const lastBtn = document.createElement("button");
        lastBtn.textContent  = ">>";
        lastBtn.addEventListener("click",()=>{
            loadItems(pageInfo.totalPages);
        })
        pagingArea.appendChild(lastBtn);


    }

}

function setIsCompletedSend(booleanVal){
    if(booleanVal)
        return "Y"
    else
        return "N"
}

function setSendBtn(card, item){

    const sendBtn = card.querySelector(".send-btn");

    // 내가 후기 작성을 한 경우
    if(item.isCompletedSend){
        sendBtn.classList.add("disabled");
    }else{
        sendBtn.addEventListener("click",()=>{
            window.location.href = `/matchup/rating/register?boardId=${item.boardId}&evalId=${item.sendedEvalId}&targetId=${item.sendedTargetId}`;
        })

    }
}

function setReceiveBtn(card, item){
    const receiveBtn = card.querySelector(".receive-btn");

    // 상대방이 후기 작성을 한 경우:
    if(item.isCompletedReceive){
        receiveBtn.addEventListener("click",()=>{
            window.location.href = `/matchup/rating/detail?boardId=${item.boardId}&evalId=${item.receivedEvalId}&targetId=${item.receivedTargetId}`;
        });
    }else{
        receiveBtn.addEventListener("click",()=>{
            alert("아직 상대방이 후기 작성을 안했습니다.");
        });

    }
}

function setReceivedScore(item){
    if(item.isCompletedReceive){
        return `
                매너 점수: ${item.receivedMannerScore} <br/>
                실력 점수: ${item.recievedSkillScore}
                `;
    }else{
        return "후기 없음";
    }
}

function setReceivedReview(item){
    if(item.isCompletedReceive){
        return item.receivedReview;
    }else{
        return "상대방이 아직 후기를 작성 안했습니다.";
    }
}


function setSendedScore(item){
    if(item.isCompletedSend){
        return `
                매너 점수: ${item.sendedMannerScore} <br/>
                실력 점수: ${item.sendedSkillScore}
                `;
    }else{
        return "후기 없음";
    }
}

function setSendedReview(item){
    if(item.isCompletedSend){
        return item.sendedReview;
    }else{
        return "후기를 작성 해주세요.";
    }
}


