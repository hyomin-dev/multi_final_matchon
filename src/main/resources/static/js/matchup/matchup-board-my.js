let myMannerTemperature;
let sportsType = '';
let dateFilter = '';
let availableFilter = false;
let lastFilterValues = {};
document.addEventListener("DOMContentLoaded",async ()=>{

    lastFilterValues={
        "sportsType": sportsType,
        "dateFilter": dateFilter,
        "availableFilter": availableFilter
    }

    document.querySelector("#sports-type").addEventListener("change",(e)=>{
        sportsType = e.target.value;
    })

    document.querySelector("#date-filter").addEventListener("change",(e)=>{
        dateFilter = e.target.value;
        //console.log(dateFilter);
    })

    document.querySelector("#availableOnly").addEventListener("change",(e)=>{
        availableFilter = e.target.checked;
        // 체크O: true
        // 체크x: false
    })

    document.querySelector("#filterBtn").addEventListener("click",()=>{
        const isSame = lastFilterValues.sportsType === sportsType &&
            lastFilterValues.dateFilter === dateFilter &&
            lastFilterValues.availableFilter === availableFilter;

        if(isSame){
            e.preventDefault();
            console.log("검색 조건이 변하지 않았습니다.");
        }else{
            lastFilterValues.sportsType = sportsType;
            lastFilterValues.dateFilter = dateFilter;
            lastFilterValues.availableFilter = availableFilter;
            loadItems(1, sportsType, dateFilter, availableFilter);
        }


    })

    loadItems(1) // 프론트는 페이지 번호 시작을 1부터, 헷갈림


})

async function loadItems(page, sportsType='', dateFilter='', availableFilter=false){
    const response = await fetch(`/matchup/board/my/list?page=${page-1}&sportsType=${sportsType}&date=${dateFilter}&availableFilter=${availableFilter}`,{
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
    renderPagination(pageInfo , sportsType, dateFilter, availableFilter);

}
function renderList(items){
    const boardArea = document.querySelector("#board-container");
    boardArea.innerHTML = '';

    if(items.length ===0){
        boardArea.innerHTML = `
            <tr>
                <td colspan="9" class="no-result"> 현재 작성된 게시글이 없습니다.</td>
            </tr>
        `;
        return;
    }


    items.forEach(item=>{
        const date = new Date(item.matchDatetime);

        const card = document.createElement("tr");
        card.innerHTML = `
                         <td>${item.boardId}</td>
                         <td>${setSportsType(item.sportsTypeName)}</td> 
                         <td class="truncate">${item.sportsFacilityAddress}</td>
                         <td>📅 ${date.getMonth() + 1}/${date.getDate()} ${date.getHours()}시 ${date.getMinutes()}분 - 
                                ${calTime(item, date.getHours(), date.getMinutes())}</td>
                         <td>${checkStatus(item)}</td>
                         <td>( ${item.currentParticipantCount} / ${item.maxParticipants} )</td>
                         <td> ${item.minMannerTemperature}</td>                         
                         <td><button onclick="location.href='/matchup/board/detail?matchup-board-id=${item.boardId}'" class="detail button-group">상세보기</button></td>
                         <td><button onclick="location.href='/matchup/request/board?board-id=${item.boardId}'" class="request button-group">요청 확인</button></td>
                         <td><button onclick="window.open('/chat/group/room?roomId=${item.roomId}', '_blank')" class="group-chat button-group">단체 채팅</button></td>
                         <td><button class="rating-setting disabled button-group">평가 세팅</button></td>     
                         
                                            
                         `;

        setRatingSettingButton(card, item);

        boardArea.appendChild(card);


    })
}

function renderPagination(pageInfo, sportsType, dateFilter, availableFilter){
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
            loadItems(1, sportsType, dateFilter, availableFilter);
        });
        pagingArea.appendChild(firstBtn);
    }

    // 이전 블록으로 이동
    if (startPage > 1){
        const prevBtn = document.createElement("button");
        prevBtn.textContent = "<";
        prevBtn.addEventListener("click",()=>{
            loadItems(startPage-1, sportsType, dateFilter, availableFilter);
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
            loadItems(i, sportsType, dateFilter, availableFilter);
        })
        pagingArea.appendChild(btn);
    }

    // 다음 블록으로 이동
    if(endPage < pageInfo.totalPages){
        const nextBtn = document.createElement("button");
        nextBtn.textContent = ">";
        nextBtn.addEventListener("click",()=>{
            loadItems(endPage+1, sportsType, dateFilter, availableFilter);
        })
        pagingArea.appendChild(nextBtn);
    }

    // 마지막 블록으로 이동

    if(endPage<pageInfo.totalPages){
        const lastBtn = document.createElement("button");
        lastBtn.textContent  = ">>";
        lastBtn.addEventListener("click",()=>{
            loadItems(pageInfo.totalPages, sportsType, dateFilter, availableFilter);
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

function checkStatus(item){

    const matchDate = new Date(item.matchDatetime);
    const now = new Date();
    const durationParts = item.matchDuration.split(":");
    const matchEnd = new Date(matchDate.getTime() + (parseInt(durationParts[0])*60+parseInt(durationParts[1])) * 60 * 1000);

    if(matchDate <now && now <= matchEnd)
        return "경기 진행";
    else if(matchEnd<now)
        return "경기 종료";
    else if(item.currentParticipantCount >= item.maxParticipants)
        return "모집 완료";
    else
        return "모집 가능"
}

function setRatingSettingButton(card, item){
    const matchDate = new Date(item.matchDatetime);
    const now = new Date();

    const durationParts = item.matchDuration.split(":");
    const matchEnd = new Date(matchDate.getTime() + (parseInt(durationParts[0])*60+parseInt(durationParts[1])) * 60 * 1000);


    if(matchEnd<now &&  !item.isRatingInitialized) {
        card.querySelector(".rating-setting").classList.remove("disabled");
        card.querySelector(".rating-setting").addEventListener("click",async ()=>{
            const response = await fetch(`/matchup/rating/setting?boardId=${item.boardId}`,{
                method: "GET",
                credentials: "include"
            })
            if(!response.ok)
                throw new Error(`HTTP error! Status:${response.status}`)
            else{
                alert("평가 세팅이 완료되었습니다.");
            }
        })
    }
}

function setSportsType(sportsTypeName){
    if(sportsTypeName ==="SOCCER"){
        return `
                <span style="color: #1abc9c;">SOCCER</span>
                `
    }else{
        return `
                <span style="color: #e67e22;">FUTSAL</span>
                `
    }
}















































