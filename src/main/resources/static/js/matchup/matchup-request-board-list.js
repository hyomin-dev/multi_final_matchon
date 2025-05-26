let boardId = '';
const Status = {
    PENDING: "PENDING",
    APPROVED: "APPROVED",
    DENIED: "DENIED",
    CANCELREQUESTED: "CANCELREQUESTED"
}


document.addEventListener("DOMContentLoaded",async ()=>{


    const detailDto = document.querySelector("#matchup-request-detail-dto");
    boardId = Number(detailDto.dataset.boardId);

    const matchDatetime = detailDto.dataset.matchDatetime;
    const matchDuration = detailDto.dataset.matchDuration;
    const currentParticipantCount = detailDto.dataset.currentParticipantCount;
    const maxParticipants = detailDto.dataset.maxParticipants;

    calTime(matchDatetime, matchDuration);
    checkMatchStatus(matchDatetime);


    loadItems(1) // 프론트는 페이지 번호 시작을 1부터, 헷갈림
})


async function loadItems(page){
    const response = await fetch(`/matchup/request/board/list?page=${page-1}&board-id=${boardId}`,{

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
    const boardArea = document.querySelector("#request-container");
    boardArea.innerHTML = '';

    items.forEach(item=>{
        const card = document.createElement("div");
        card.className = "matchup-card";
        card.innerHTML = `
            <div class="card-content">
                <div class="left-info">
                    <div><strong><a href="/matchup/request/detail?request-id=${item.requestId}">상세보기</a></strong></div>
                    <div><strong>신청자: ${item.applicantName}</strong></div>
                    <div><strong>신청 인원: ${item.participantCount}</strong></div>
                    <div><strong>요청 횟수: ${item.matchupRequestSubmittedCount}</strong></div>
                    <div><strong>승인 취소 요청 횟수 : ${item.matchupCancelSubmittedCount}</strong></div>
                    <div><strong>상태: ${manageRequestInfo(item)}</strong></div>
                </div>               
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

function calTime(matchDatetime, matchDuration){
    //console.log(matchDatetime);
    //console.log(matchDuration);

    const date = new Date(matchDatetime);
    //console.log(date);
    const matchDateEle = document.querySelector("#match-date");

    const month = date.getMonth()+1;
    const day = date.getDate();

    const startHour = date.getHours();
    const startMinutes = date.getMinutes();


    const [hour, minute, second] = matchDuration.split(":");
    const hourNum = parseInt(hour, 10);
    const minuteNum = parseInt(minute,10);

    let extraHour = 0
    let endMinute = 0;

    if(date.getMinutes()+minuteNum>=60){
        extraHour = 1;
        endMinute = (date.getMinutes()+minuteNum)%60;
    }else{
        endMinute = date.getMinutes()+minuteNum;
    }

    if(startHour+hourNum+extraHour>=24)
        endHour = (startHour+hourNum+extraHour) %24;
    else
        endHour = startHour+hourNum+extraHour;

    matchDateEle.textContent = `${month}/${day} ${startHour}시 ${startMinutes}분 - ${endHour}시 ${endMinute}분`

}

function checkMatchStatus(matchDatetime){
    const matchStatusEle = document.querySelector("#match-status");

    const matchDate = new Date(matchDatetime);
    const now = new Date();
    if(matchDate<now)
        matchStatusEle.textContent = "경기 종료";
    else
        matchStatusEle.textContent =  "경기 시작전";
}

function manageRequestInfo(item){
    const matchDate = new Date(item.matchDatetime);
    const now = new Date();

    // console.log(item.matchupStatus);
    // console.log(item.matchupRequestSubmittedCount);
    // console.log(item.matchupCancelSubmittedCount);
    // console.log(item.isDeleted);
    // if(item.matchupStatus === Status.PENDING)
    //     console.log("enum 사용");

    // 1. 참가 요청 후 승인 대기
    if(
        (item.matchupStatus ===Status.PENDING && item.matchupRequestSubmittedCount===1 && item.matchupCancelSubmittedCount===0 && item.isDeleted===false) ||
        (item.matchupStatus===Status.PENDING && item.matchupRequestSubmittedCount===2 && item.matchupCancelSubmittedCount===0 && item.isDeleted ===false)
    ){
        return "승인 대기";
    }
    // 2. 참가 요청 삭제
    else if(
        (item.matchupStatus===Status.PENDING && item.matchupRequestSubmittedCount===1 && item.matchupCancelSubmittedCount===0 && item.isDeleted===true) ||
        (item.matchupStatus===Status.PENDING && item.matchupRequestSubmittedCount===2 && item.matchupCancelSubmittedCount===0 && item.isDeleted===true) ||
        (item.matchupStatus===Status.DENIED && item.matchupRequestSubmittedCount===1 && item.matchupCancelSubmittedCount===0 && item.isDeleted===true)
    ){
        return "요청 삭제됨";
    }
    // 3. 참가 요청 승인
    else if(
        (item.matchupStatus===Status.APPROVED && item.matchupRequestSubmittedCount===2 && item.matchupCancelSubmittedCount===0 && item.isDeleted===false)||
        (item.matchupStatus===Status.APPROVED && item.matchupRequestSubmittedCount===1 && item.matchupCancelSubmittedCount===0 && item.isDeleted===false)
    ){
        return "승인됨";
    }
    // 4. 참가 요청 반려
    else if(
        (item.matchupStatus === Status.DENIED && item.matchupRequestSubmittedCount ===2 && item.matchupCancelSubmittedCount ===0 && item.isDeleted ===false) ||
        (item.matchupStatus === Status.DENIED && item.matchupRequestSubmittedCount ===1 && item.matchupCancelSubmittedCount ===0 && item.isDeleted ===false)
    ){
        return "반려됨";
    }
    // 8. 승인 취소 요청을 했으나 경기 시간이 지나 자동 참가 처리
    else if(
        (matchDate <= now) &&
        (
            (item.matchupStatus === Status.CANCELREQUESTED && item.matchupRequestSubmittedCount ===2 && item.matchupCancelSubmittedCount ===1 && item.isDeleted===false) ||
            (item.matchupStatus === Status.CANCELREQUESTED && item.matchupRequestSubmittedCount ===1 && item.matchupCancelSubmittedCount ===1 && item.isDeleted===false)
        )
    ){
        return "자동 참가"
    }
    // 5. 승인 취소 요청 상태
    else if(
        (item.matchupStatus === Status.CANCELREQUESTED && item.matchupRequestSubmittedCount ===2 && item.matchupCancelSubmittedCount ===1 && item.isDeleted===false) ||
        (item.matchupStatus === Status.CANCELREQUESTED && item.matchupRequestSubmittedCount ===1 && item.matchupCancelSubmittedCount ===1 && item.isDeleted===false)
    ){
        return "승인 취소 요청";
    }
    // 6. 승인 취소 요청이 승인
    else if(
        (item.matchupStatus===Status.CANCELREQUESTED && item.matchupRequestSubmittedCount === 2 && item.matchupCancelSubmittedCount===1 && item.isDeleted===true) ||
        (item.matchupStatus===Status.CANCELREQUESTED && item.matchupRequestSubmittedCount === 1 && item.matchupCancelSubmittedCount===1 && item.isDeleted===true)
    ){
        return "취소 요청 승인";
    }
    // 7. 승인 취소 요청이 반려
    else if(
        (item.matchupStatus===Status.APPROVED && item.matchupRequestSubmittedCount===2 && item.matchupCancelSubmittedCount===1 && item.isDeleted ===false) ||
        (item.matchupStatus===Status.APPROVED && item.matchupRequestSubmittedCount===1 && item.matchupCancelSubmittedCount===1 && item.isDeleted ===false)
    ){
        return "취소 요청 반려";
    }else{
        return "서버 오류";
    }





}
