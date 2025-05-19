document.addEventListener("DOMContentLoaded",()=>{
    loadItems(1) // 프론트는 페이지 번호 시작을 1부터, 헷갈림


    // document.querySelector("#register").addEventListener("click",()=>{
    //
    // })
})

async function loadItems(page){
    const response = await fetch(`/matchup/board/list?page=${page-1}`,{
        method: "GET",
        headers: {
            "Authorization": "Bearer " + localStorage.getItem("accessToken")
        }
    });
    if(!response.ok)
        throw new Error(`HTTP error! Status:${response.status}`)
    const data = await response.json();
    console.log(data);
    const items = data.data.items;
    const pageInfo = data.data.pageInfo;
    console.log(pageInfo);

    renderList(items);
    renderPagination(pageInfo)

}
function renderList(items){
    const boardArea = document.querySelector("#board-container");
    boardArea.innerHTML = '';

    items.forEach(item=>{
        const date = new Date(item.matchDatetime);
        const month = date.getMonth()+1;
        //const

        const card = document.createElement("div");
        card.className = "matchup-card";
        card.innerHTML = `
            <div class="matchup-left">
              <div class="team-name">${item.teamName}</div>
              <button class="inquiry-btn">문의 하기</button>
              <div class="board-id">${item.boardId}</div>
              
            </div>
        
            <div class="matchup-info">                
              <div class="nickname">${item.writer}</div>
              <div class="sports-type">${item.sportsTypeName}</div>
              <div class="sports-facility">${item.sportsFacilityName}</div>
              <div class="match-datetime">
                <span class="match-date">📅 ${item.matchDatetime}</span>
                <span class="match-time">⏰ ${item.matchDuration}</span>
              </div>
              <div class="manner-graph">
                <div class="text">입장 가능 온도</div>
                <div class="line">
                  <span class="min">${item.minMannerTemperature}°C</span>
                  <span class="triangle">▼</span>
                  <span class="current">${item.currentParticipantCount}°C</span>
                </div>
              </div>
            </div>
        
            <div class="matchup-right">
              <div class="status">${item.currentParticipantCount >=item.maxParticipants ? "신청 마감": "신청 가능"}</div>
              <div class="participants">(${item.maxParticipants})</div>
            </div>
          `;
        boardArea.appendChild(card);

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
            btn.disabled = true;

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


// function renderPagination(pageInfo){
//     const pagingArea = document.querySelector("#paging-container");
//
//     pagingArea.innerHTML = '';
//
//     for(let i=1;i<=pageInfo.totalPages;i++){
//         const btn = document.createElement("button");
//         btn.textContent = i;
//         if(i===(pageInfo.page+1))
//             btn.disabled = true;
//         btn.addEventListener("click",()=>{
//             loadItems(i-1);
//         })
//         pagingArea.appendChild(btn);
//     }
// }












































