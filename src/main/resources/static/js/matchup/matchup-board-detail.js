document.addEventListener("DOMContentLoaded",()=>{
        setContent();

})


async function setContent(){
    const detailDto = document.querySelector("#matchup-board-detail-dto");

    const boardId = Number(detailDto.dataset.boardId);
    const writer = detailDto.dataset.writer;
    const sportsFacilityName = detailDto.dataset.sportsFacilityName;
    const sportsFacilityAddress = detailDto.dataset.sportsFacilityAddress;
    const matchDatetime = detailDto.dataset.matchDatetime;
    const matchDuration = detailDto.dataset.matchDuration;
    const currentParticipantCount = Number(detailDto.dataset.currentParticipantCount);
    const maxParticipants = Number(detailDto.dataset.maxParticipants);
    const minMannerTemperature = Number(detailDto.dataset.minMannerTemperature);
    const originalName = detailDto.dataset.originalName;
    const savedName = detailDto.dataset.savedName;
    const savedPath = detailDto.dataset.savedPath;
    const myTemperature = Number(detailDto.dataset.myTemperature);
    const loginMember = detailDto.dataset.loginMember;
    //const baseUrl = detailDto.dataset.baseUrl;
    //console.log(sportsFacilityAddress);
    //console.log(matchDatetime);

    setWriter(writer, loginMember);
    drawMap(sportsFacilityAddress, sportsFacilityName);
    calTime(matchDatetime, matchDuration);
    checkStatus(matchDatetime, currentParticipantCount, maxParticipants, writer, loginMember, minMannerTemperature, myTemperature);
    setButton(matchDatetime, writer, loginMember);


    const response = await fetch(`/matchup/attachment/presigned-url?saved-name=${savedName}`,{
        method: "GET",
        credentials: "include"
        })
    if(!response.ok)
        throw new Error(`HTTP error! Status:${response.status}`)
    const data = await response.json();
    //console.log(data.data);
    document.querySelector("#reservationUrl").href = data.data;


    //아래는 CORS 막아놔서 안됨
    // const response2 = await fetch(data.data);
    // if(!response2.ok)
    //     throw new Error(`HTTP error! Status:${response.status}`)
    // const data2 = await response2.json();
    // console.log(data2);


}

function setWriter(writer, loginMember){
    const writerEle = document.querySelector("#writer");
    if(writer===loginMember)
        writerEle.innerHTML = "나";
    else
        writerEle.innerHTML = writer;
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

function checkStatus(matchDatetime, currentParticipantCount, maxParticipants, writer, loginMember, minMannerTemperature, myTemperature){
    const statusEle = document.querySelector("#status");

    // console.log(currentParticipantCount)
    // console.log(maxParticipants)

    // 공통: 경기 날짜 지나면 경기종료
    const matchDate = new Date(matchDatetime);
    const now = new Date();
    if(matchDate<now)
        statusEle.innerHTML = "경기 종료"
    else if(writer === loginMember){
        // 경우1: 사용자가 쓴 글
        // 신청 가능 여부: 모집 중, 모집 완료, 경기 종료

        if(currentParticipantCount < maxParticipants)
            statusEle.innerHTML =  "모집 가능";
        else
            statusEle.innerHTML = "모집 완료";
    }else{
        // 경우2: 다른 사람의 글
        // 신청 가능 여부: 신청 가능, 신청 마감, 입장 불가, 경기 종료
        if(minMannerTemperature>myTemperature)
            statusEle.innerHTML = "입장 불가";
        else if(currentParticipantCount < maxParticipants)
            statusEle.innerHTML = "신청 가능";
        else
            statusEle.innerHTML = "신청 불가";
    }
}

function setButton(matchDatetime, writer, loginMember){
    const matchDate = new Date(matchDatetime);
    const now = new Date();
    if(writer === loginMember && matchDate<now){
       const modifyBtn = document.querySelector("#modify");
       modifyBtn.addEventListener("click",(e)=>{
           alert("경기 시작 시간이 지나 수정할 수 없습니다.");
           e.preventDefault();
       })
    }
}



























