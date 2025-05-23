let sportsType = '';
let dateFilter = '';

document.addEventListener("DOMContentLoaded",async ()=>{
    document.querySelector("#sports-type").addEventListener("change",(e)=>{
        sportsType = e.target.value;
    })

    document.querySelector("#date-filter").addEventListener("change",(e)=>{
        dateFilter = e.target.value;
        //console.log(dateFilter);
    })
    document.querySelector("#filterBtn").addEventListener("click",()=>{
        loadItems(1, sportsType, dateFilter);
    })
    loadItems(1) // 프론트는 페이지 번호 시작을 1부터, 헷갈림
})


async function loadItems(page, sportsType='', dateFilter=''){
    const response = await fetch(`/matchup/request/my/list?page=${page-1}&sportsType=${sportsType}&date=${dateFilter}`,{

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
    renderPagination(pageInfo,sportsType, dateFilter);


}
function renderList(items){
    const boardArea = document.querySelector("#request-container");
    boardArea.innerHTML = '';

    items.forEach(item=>{
        const date = new Date(item.matchDatetime);

        const card = document.createElement("div");
        card.className = "matchup-card";
        card.innerHTML = `
            <div class="card-content">
                <div class="left-info">
                    <div><strong>신청 인원: ${item.participantCount}</strong></div>
                    <div><strong>요청 상태: ${item.matchupStatus}</strong></div>
                    <div><strong>경기 상태: ${checkMatchStatus(item)}</strong></div>
                    <div class="button-group">
                        <a href="/matchup/board/detail?matchup-board-id=${item.boardId}">
                            <button class="detail">게시글 상세보기</button>
                        </a>
                        <a href="/matchup/request/detail?request-id=${item.requestId}">
                            <button class="detail">요청 상세보기</button>
                        </a>
                    </div>
                </div>
                <div class="right-info">
                    <div><strong>종목: ${item.sportsTypeName}</strong></div>
                    <div><strong>경기장: ${item.sportsFacilityName}</strong></div>
                    <div>경기장 주소: ${item.sportsFacilityAddress}</div>
                    <div>📅 날짜: ${date.getMonth()+1}/${date.getDate()} ${date.getHours()}시 ${date.getMinutes()}분 - ${calTime(item,date.getHours(), date.getMinutes())}</div>
                </div>
            </div>       
                `;
        boardArea.appendChild(card);

    })
}

function renderPagination(pageInfo, sportsType, dateFilter){

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
            loadItems(1, sportsType, dateFilter);

        });
        pagingArea.appendChild(firstBtn);
    }

    // 이전 블록으로 이동
    if (startPage > 1){
        const prevBtn = document.createElement("button");
        prevBtn.textContent = "<";
        prevBtn.addEventListener("click",()=>{
            loadItems(startPage-1, sportsType, dateFilter);

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
            loadItems(i,sportsType, dateFilter);
        })
        pagingArea.appendChild(btn);
    }

    // 다음 블록으로 이동
    if(endPage < pageInfo.totalPages){
        const nextBtn = document.createElement("button");
        nextBtn.textContent = ">";
        nextBtn.addEventListener("click",()=>{
            loadItems(endPage+1, sportsType, dateFilter);

        })
        pagingArea.appendChild(nextBtn);
    }

    // 마지막 블록으로 이동

    if(endPage<pageInfo.totalPages){
        const lastBtn = document.createElement("button");
        lastBtn.textContent  = ">>";
        lastBtn.addEventListener("click",()=>{
            loadItems(pageInfo.totalPages, sportsType, dateFilter);
        })
        pagingArea.appendChild(lastBtn);


    }

}

function calTime(item, startHour, startMinute){


    const [hour, minute, second] = item.matchDuration.split(":");
    const hourNum = parseInt(hour, 10);
    const minuteNum = parseInt(minute,10);
    let extraHour = 0
    let endMinute = 0;
    if(startMinute+minuteNum>=60){
        extraHour = 1;
        endMinute = (startMinute+minuteNum)%60;
    }else{
        endMinute = startMinute+minuteNum;
    }

    if(startHour+hourNum+extraHour>=24)
        endHour = (startHour+hourNum+extraHour) %24;
    else
        endHour = startHour+hourNum+extraHour;

    return `${endHour}시 ${endMinute}분`

}

function checkMatchStatus(item){
    const matchDate = new Date(item.matchDatetime);
    const now = new Date();
    if(matchDate<now)
        return "경기 종료"
    else
        return "경기 시작전"
}