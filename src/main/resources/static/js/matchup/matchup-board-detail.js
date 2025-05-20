document.addEventListener("DOMContentLoaded",()=>{
        setContent();

})


async function setContent(){
    const detailDto = document.querySelector("#matchup-board-detail-dto");

    const boardId = detailDto.dataset.boardId;
    const writer = detailDto.dataset.writer;
    const sportsFacilityName = detailDto.dataset.sportsFacilityName;
    const sportsFacilityAddress = detailDto.dataset.sportsFacilityAddress;
    const matchDatetime = detailDto.dataset.matchDatetime;
    const matchDuration = detailDto.dataset.matchDuration;
    const currentParticipantCount = detailDto.dataset.currentParticipantCount;
    const maxParticipants = detailDto.dataset.maxParticipants;
    const minMannerTemperature = detailDto.dataset.minMannerTemperature;
    const originalName = detailDto.dataset.originalName;
    const savedName = detailDto.dataset.savedName;
    const savedPath = detailDto.dataset.savedPath;
    const myTemperature = detailDto.dataset.myTemperature;
    const baseUrl = detailDto.dataset.baseUrl;
    //console.log(sportsFacilityAddress);
    //console.log(matchDatetime);

    drawMap(sportsFacilityAddress, sportsFacilityName);
    calTime(matchDatetime, matchDuration);
    calParticipants(currentParticipantCount, maxParticipants);

    // const response = await fetch(`/get/attachment?saved-name=${savedName}`,{
    //     method: "GET",
    //     credentials: "include"
    //     })
    // if(!response.ok)
    //     throw new Error(`HTTP error! Status:${response.status}`)
    // const data = await response.json();
    // console.log(data);
}

function drawMap(address){
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
                content: '<div style="width:150px;text-align:center;padding:6px 0;">${sportsFacilityName}</div>'
            });
            infowindow.open(map, marker);

            // 지도의 중심을 결과값으로 받은 위치로 이동시킵니다
            map.setCenter(coords);
        }
    });
}

function calTime(matchDatetime, matchDuration){
    //console.log(matchDatetime);
    console.log(matchDuration);

    const date = new Date(matchDatetime);
    console.log(date);
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

function calParticipants(currentParticipantCount, maxParticipants){

}



























