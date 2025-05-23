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


    drawMap(sportsFacilityAddress, sportsFacilityName);
    calTime(matchDatetime, matchDuration);
    setParticipantCount(currentParticipantCount, maxParticipants, participantCount);

})

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