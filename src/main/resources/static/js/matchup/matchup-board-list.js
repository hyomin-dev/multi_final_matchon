document.addEventListener("DOMContentLoaded",()=>{
    initList()


    document.querySelector("#register").addEventListener("click",()=>{

    })
})

async function initList(){
    const response = await fetch("/matchup/board/list?page=0",{
        method: "GET",
        headers: {
            "Authorization": "Bearer " + localStorage.getItem("accessToken")
        }
    });
    if(!response.ok)
        throw new Error(`HTTP error! Status:${response.status}`)
    const data = await response.json();
    console.log(data);
    const content = data.data.content;
    const pageInfo = data.data.pageInfo;
    //console.log(content);
    console.log(pageInfo);


    const boardArea = document.querySelector("#board-container");
    const pagingArea = document.querySelector("#paging-container");

    content.forEach(c=>{
        const card = document.createElement("div");
        card.className = "matchup-card";
        card.innerHTML = `
            <div class="matchup-left">
              <div class="team-name">${c.teamName}</div>
              <button class="inquiry-btn">문의 하기</button>
            </div>
        
            <div class="matchup-info">
              <div class="nickname">${c.writer}</div>
              <div class="sports-type">${c.sportsTypeName}</div>
              <div class="sports-facility">${c.sportsFacilityName}</div>
              <div class="match-datetime">
                <span class="match-date">📅 ${c.matchDatetime}</span>
                <span class="match-time">⏰ ${c.matchDuration}</span>
              </div>
              <div class="manner-graph">
                <div class="text">입장 가능 온도</div>
                <div class="line">
                  <span class="min">${c.minMannerTemperature}°C</span>
                  <span class="triangle">▼</span>
                  <span class="current">${c.currentParticipantCount}°C</span>
                </div>
              </div>
            </div>
        
            <div class="matchup-right">
              <div class="status">${c.currentParticipantCount >=c.maxParticipants ? "신청 마감": "신청 가능"}</div>
              <div class="participants">(${c.maxParticipants})</div>
            </div>
          `;
        boardArea.appendChild(card);

    })

}

