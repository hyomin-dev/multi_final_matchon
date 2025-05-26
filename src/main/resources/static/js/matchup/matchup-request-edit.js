const Status = {
    PENDING: "PENDING",
    APPROVED: "APPROVED",
    DENIED: "DENIED",
    CANCELREQUESTED: "CANCELREQUESTED"
}

document.addEventListener("DOMContentLoaded",()=>{
    const registerDto = document.querySelector("#matchup-request-register-dto");

    const sportsTypeName = registerDto.dataset.sportsTypeName;
    const sportsFacilityName = registerDto.dataset.sportsFacilityName;
    const sportsFacilityAddress = registerDto.dataset.sportsFacilityAddress;
    const matchDatetime = registerDto.dataset.matchDatetime;
    const matchDuration = registerDto.dataset.matchDuration;
    const currentParticipantCount = Number(registerDto.dataset.currentParticipantCount);
    const maxParticipants = Number(registerDto.dataset.maxParticipants);
    const participantCount = Number(registerDto.dataset.participantCount);

    const matchupStatus = registerDto.dataset.matchupStatus;
    const matchupRequestSubmittedCount = Number(registerDto.dataset.matchupRequestSubmittedCount);
    const matchupCancelSubmittedCount = Number(registerDto.dataset.matchupCancelSubmittedCount);
    const isDeleted = registerDto.dataset.isDeleted  === "true";


    drawMap(sportsFacilityAddress, sportsFacilityName);
    calTime(matchDatetime, matchDuration);
    setParticipantCount(currentParticipantCount, maxParticipants, participantCount);
    manageRequestInfo(matchupStatus, matchupRequestSubmittedCount, matchupCancelSubmittedCount, isDeleted, matchDatetime);

    const form = document.querySelector("form");
    form.addEventListener("submit",(e)=>{
        submitCheck(e, matchDatetime);
    })

    const cancel = document.querySelector(".delete-btn");
    cancel.addEventListener("click",()=>{
        history.back();
    })

})

function submitCheck(e, matchDatetime){
    const selfIntroEle = document.querySelector("#selfIntro");
    const participantCountEle = document.querySelector("#participantCount");
    const date = new Date(matchDatetime);
    const now = new Date();

    if(selfIntroEle.value ===""){
        alert("자기 소개를 입력하세요.");
        e.preventDefault();
    }else if(participantCountEle.value === ""){
        alert("참가 인원을 입력하세요.");
        e.preventDefault();
    }else if(date<now){
        alert("경기 시작 시간이 지나 수정할 수 없습니다.");
        e.preventDefault();
    }else{
        alert("submit");
    }

}

function drawMap(address, sportsFacilityName){
    var mapContainer = document.getElementById('map'), // 지도를 표시할 div
        mapOption = {
            center: new kakao.maps.LatLng(33.450701, 126.570667), // 지도의 중심좌표
            level: 3 // 지도의 확대 레벨
        };

// 지도를 생성합니다
    var map = new kakao.maps.Map(mapContainer, mapOption);

// 주소-좌표 변환 객체를 생성합니다
    var geocoder = new kakao.maps.services.Geocoder();

// 주소로 좌표를 검색합니다
    geocoder.addressSearch(address, function(result, status) {

        // 정상적으로 검색이 완료됐으면
        if (status === kakao.maps.services.Status.OK) {

            var coords = new kakao.maps.LatLng(result[0].y, result[0].x);

            // 결과값으로 받은 위치를 마커로 표시합니다
            var marker = new kakao.maps.Marker({
                map: map,
                position: coords
            });

            // 인포윈도우로 장소에 대한 설명을 표시합니다
            var infowindow = new kakao.maps.InfoWindow({
                content: '<div style="width:150px;text-align:center;padding:6px 0;">'+sportsFacilityName+'</div>'
            });
            infowindow.open(map, marker);

            // 지도의 중심을 결과값으로 받은 위치로 이동시킵니다
            map.setCenter(coords);
        }
    });
}

function calTime(matchDatetime, matchDuration){
    // console.log(matchDatetime);
    // console.log(matchDuration);

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


function setParticipantCount(currentParticipantCount, maxParticipants, participantCount){

    const participantCountEle = document.querySelector("#participantCount");

    for(let i=1; i<=(maxParticipants-currentParticipantCount);i++){
        const option = document.createElement("option");
        option.value = i;
        option.textContent = i;
        if(i === participantCount)
            option.selected = true;
        participantCountEle.appendChild(option);

    }
}

function manageRequestInfo(matchupStatus, matchupRequestSubmittedCount, matchupCancelSubmittedCount, isDeleted, matchDatetime){
    const matchDate = new Date(matchDatetime);
    const now = new Date();

    //console.log(matchDate<now);

    const statusEle = document.querySelector("#status");

    // console.log(matchupStatus);
    // console.log(matchupRequestSubmittedCount);
    // console.log(matchupCancelSubmittedCount);
    // console.log(isDeleted);
    // if(matchupStatus === Status.PENDING)
    //     console.log("enum 사용");
    // console.log(matchupStatus ===Status.PENDING && matchupRequestSubmittedCount===1 && matchupCancelSubmittedCount===0 && isDeleted===false);

    // 1. 참가 요청 후 승인 대기
    if(
        (matchupStatus ===Status.PENDING && matchupRequestSubmittedCount===1 && matchupCancelSubmittedCount===0 && isDeleted===false) ||
        (matchupStatus===Status.PENDING && matchupRequestSubmittedCount===2 && matchupCancelSubmittedCount===0 && isDeleted ===false)
    ){
        statusEle.textContent =  "승인 대기";
    }
    // 2. 참가 요청 삭제
    else if(
        (matchupStatus===Status.PENDING && matchupRequestSubmittedCount===1 && matchupCancelSubmittedCount===0 && isDeleted===true) ||
        (matchupStatus===Status.PENDING && matchupRequestSubmittedCount===2 && matchupCancelSubmittedCount===0 && isDeleted===true) ||
        (matchupStatus===Status.DENIED && matchupRequestSubmittedCount===1 && matchupCancelSubmittedCount===0 && isDeleted===true)
    ){
        statusEle.textContent =  "요청 취소됨";
    }
    // 3. 참가 요청 승인
    else if(
        (matchupStatus===Status.APPROVED && matchupRequestSubmittedCount===1 && matchupCancelSubmittedCount===0 && isDeleted===false)||
        (matchupStatus===Status.APPROVED && matchupRequestSubmittedCount===2 && matchupCancelSubmittedCount===0 && isDeleted===false)
    ){
        statusEle.textContent = "승인됨";
    }
    // 4. 참가 요청 반려
    else if(
        (matchupStatus === Status.DENIED && matchupRequestSubmittedCount ===1 && matchupCancelSubmittedCount ===0 && isDeleted ===false) ||
        (matchupStatus === Status.DENIED && matchupRequestSubmittedCount ===2 && matchupCancelSubmittedCount ===0 && isDeleted ===false)
    ){
        statusEle.textContent = "반려됨";
    }
    // 8. 승인 취소 요청을 했으나 경기 시간이 지나 자동 참가 처리
    else if(
        (matchDate <now) &&
        (
            (matchupStatus === Status.CANCELREQUESTED && matchupRequestSubmittedCount ===2 && matchupCancelSubmittedCount ===1 && isDeleted===false) ||
            (matchupStatus === Status.CANCELREQUESTED && matchupRequestSubmittedCount ===1 && matchupCancelSubmittedCount ===1 && isDeleted===false)
        )
    ){
        statusEle.textContent = "자동 참가"
    }
    // 5. 승인 취소 요청 상태
    else if(
        (matchupStatus === Status.CANCELREQUESTED && matchupRequestSubmittedCount ===2 && matchupCancelSubmittedCount ===1 && isDeleted===false) ||
        (matchupStatus === Status.CANCELREQUESTED && matchupRequestSubmittedCount ===1 && matchupCancelSubmittedCount ===1 && isDeleted===false)
    ){
        statusEle.textContent = "승인 취소 요청";
    }
    // 6. 승인 취소 요청이 승인
    else if(
        (matchupStatus===Status.CANCELREQUESTED && matchupRequestSubmittedCount === 2 && matchupCancelSubmittedCount===1 && isDeleted===true) ||
        (matchupStatus===Status.CANCELREQUESTED && matchupRequestSubmittedCount === 1 && matchupCancelSubmittedCount===1 && isDeleted===true)
    ){
        statusEle.textContent = "취소 요청 승인";
    }
    // 7. 승인 취소 요청이 반려
    else if(
        (matchupStatus===Status.APPROVED && matchupRequestSubmittedCount===2 && matchupCancelSubmittedCount===1 && isDeleted ===false) ||
        (matchupStatus===Status.APPROVED && matchupRequestSubmittedCount===1 && matchupCancelSubmittedCount===1 && isDeleted ===false)
    ){
        statusEle.textContent = "취소 요청 반려";
    }else{
        statusEle.textContent = "서버 오류";
    }
}