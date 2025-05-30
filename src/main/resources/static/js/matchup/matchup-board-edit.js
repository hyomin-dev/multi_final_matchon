document.addEventListener("DOMContentLoaded",()=>{

    const editDto = document.querySelector("#matchup-board-edit-dto");

    const boardId = Number(editDto.dataset.boardId);
    const sportsTypeName = editDto.dataset.sportsTypeName;
    const currentParticipantCount = Number(editDto.dataset.currentParticipantCount);
    const maxParticipants = Number(editDto.dataset.maxParticipants);
    const minMannerTemperature = Number(editDto.dataset.minMannerTemperature);
    const originalName = editDto.dataset.originalName;
    const savedName = editDto.dataset.savedName;
    const myMannerTemperature = Number(editDto.dataset.myMannerTemperature);
    const loginMember = editDto.dataset.loginMember;

    setSportsType(sportsTypeName); // 종목 가져옴
    setReservationFile(originalName, savedName);
    setMaxParticipants(currentParticipantCount, maxParticipants);
    setMannerTemperature(minMannerTemperature);
    setButton();

    const form = document.querySelector("form");
    form.addEventListener("submit", (e)=>{
        submitCheck(e, myMannerTemperature);
    })

    const backBtn = document.querySelector(".back-btn");
    backBtn.addEventListener("click",()=>{
        history.back();
    });

})

function submitCheck(e, myMannerTemperature){

    const teamNameEle = document.querySelector("#teamName");

    const teamIntroEle = document.querySelector("#teamIntro");

    const sportsFacilityNameEle = document.querySelector("#sportsFacilityName");

    const sportsFacilityAddressEle = document.querySelector("#sportsFacilityAddress");

    const matchDatetimeEle = document.querySelector("#matchDatetime");

    const matchDurationEle = document.querySelector("#matchDuration");

    const currentParticipantCountEle = document.querySelector("#currentParticipantCount");
    //console.log(currentParticipantsCountEle.value);

    const maxParticipantsEle = document.querySelector("#maxParticipants");
    //console.log(maxParticipantsEle.value);

    const minMannerTemperatureEle = document.querySelector("#minMannerTemperature");
    //console.log(minMannerTemperatureEle.value);
    //console.log(myMannerTemperature);

    const matchDescriptionEle = document.querySelector("#matchDescription");

    //console.log(matchDatetimeEle.value);
    const matchDate = new Date(matchDatetimeEle.value);
    const now = new Date();
    // if(matchDate < now){
    //     console.log("test");
    // }

   if(teamNameEle.value === ""){
        alert("소속 팀이 있어야 합니다.");
        e.preventDefault();
    } else if(teamIntroEle.value ===""){
        alert("팀 소개를 입력하세요.");
        e.preventDefault();
    } else if(sportsFacilityNameEle.value ===""){
        alert("경기장 명을 입력하세요.");
        e.preventDefault();
    } else if(sportsFacilityAddressEle.value ===""){
        alert("경기장 주소를 입력하세요.");
        e.preventDefault();
    } else if(matchDatetimeEle.value ===""){
        alert("경기 날짜를 입력하세요");
        e.preventDefault();
    }  else if(matchDate<now){
        alert("경기 시작 시간이 지나 수정할 수 없습니다.");
        e.preventDefault();
    } else if(matchDurationEle.value ===""){
        alert("경기 진행 시간을 입력하세요.");
        e.preventDefault();
    } else if(currentParticipantCountEle.value ===""){
        alert("현재 참가 인원을 입력하세요.")
        e.preventDefault();
    } else if(maxParticipantsEle.value ===""){
        alert("총 모집 인원을 입력하세요.");
        e.preventDefault();
    }else if(Number(currentParticipantCountEle.value) >Number(maxParticipantsEle.value)){
       alert(`현재 참가 인원은 총 모집 인원보다 적어야 합니다.`)
       e.preventDefault();
   } else if(minMannerTemperatureEle.value ===""){
        alert("하한 매너 온도를 입력하세요.");
        e.preventDefault();
    } else if(minMannerTemperatureEle.value>myMannerTemperature){
       alert(`하한 매너 온도는 내 매너 온도 ${myMannerTemperature} 이상이어야 합니다.`);
       e.preventDefault();
   } else if(matchDescriptionEle.value ===""){
        alert("경기 방식 소개를 입력하세요");
        e.preventDefault();
    } else{
        alert("submit");

    }
}

async function setSportsType(sportsTypeName){
    const optionSoccerEle = document.querySelector("#option-soccer");
    const optionFutsal = document.querySelector("#option-futsal");

    if(optionSoccerEle.value === sportsTypeName)
        optionSoccerEle.selected = true;
    else
        optionFutsal.selected = true;
}

async function setReservationFile(originalName, savedName){
    const response2 = await fetch(`/matchup/attachment/file?saved-name=${savedName}`,{
        method: "GET",
        credentials: "include"
    })
    if(!response2.ok)
        throw new Error(`HTTP error! Status:${response2.status}`)
    const data2 = await response2.blob();
    const url = window.URL.createObjectURL(data2);
    const aEle = document.querySelector("#reservationLoadBox > span > a");


    aEle.href = url;
    aEle.download = originalName;

    aEle.addEventListener("click",()=>{
        setTimeout(()=>{
            aEle.removeAttribute("href")
            URL.revokeObjectURL(url);
        },500)
    })

    // const aEle2 = document.createElement("a");
    // aEle2.innerHTML = "삭제하기";
    //
    // reservationBoxEle.appendChild(aEle2);

}


function getAddress() {
    new daum.Postcode({
        oncomplete: function(data) {
            // 팝업에서 검색결과 항목을 클릭했을때 실행할 코드를 작성하는 부분.

            // 각 주소의 노출 규칙에 따라 주소를 조합한다.
            // 내려오는 변수가 값이 없는 경우엔 공백('')값을 가지므로, 이를 참고하여 분기 한다.
            var addr = ''; // 주소 변수

            //사용자가 선택한 주소 타입에 따라 해당 주소 값을 가져온다.
            if (data.userSelectedType === 'R') { // 사용자가 도로명 주소를 선택했을 경우
                addr = data.roadAddress;
            } else { // 사용자가 지번 주소를 선택했을 경우(J)
                addr = data.jibunAddress;
            }

            document.querySelector("#sportsFacilityAddress").value = addr;
            // 커서를 상세주소 필드로 이동한다.
            document.querySelector("#sportsFacilityAddress").focus();

        }
    }).open();
}


function setMaxParticipants(currentParticipantCount, maxParticipants){

    const selectMax = document.querySelector("#maxParticipants");

    for(let i=currentParticipantCount; i<=30;i++){
        const option = document.createElement("option");
        option.value = i;
        option.textContent = i;
        selectMax.appendChild(option);
        if(i===maxParticipants)
            option.selected = true;
    }
}

function setMannerTemperature(minMannerTemperature){
    const selectManner = document.querySelector("#minMannerTemperature");

    for(let i=30;i<=40;i+=0.5){
        const option = document.createElement("option");
        option.value = i;
        option.textContent = i;
        selectManner.appendChild(option);
        if(i===minMannerTemperature)
            option.selected = true;
    }
}

async function setButton(){
    // const deleteBtn = document.querySelector(".delete-btn");
    // deleteBtn.addEventListener("click",async ()=>{
    //     const response = await fetch(`/matchup/board/delete?boardId=${boardId}`,{
    //         method: "GET",
    //         credentials: "include"
    //     })
    //     if(!response.ok)
    //         throw new Error(`HTTP error! Status:${response.status}`)
    //     alert("삭제 완료");
    //     window.location.href="/matchup";
    // })

    const toggleBtn = document.querySelector("#toggleBtn");
    const reservationLoadBox = document.querySelector("#reservationLoadBox");
    const reservationFileBox = document.querySelector("#reservationFileBox");

    toggleBtn.addEventListener("click",()=>{
        const isDelete = toggleBtn.textContent === "파일 변경";

        if(isDelete){
            toggleBtn.textContent = "변경 취소";
            reservationLoadBox.style.display = "none";
            reservationFileBox.style.display = "block";
            document.querySelector("#reservationFileBox > input").required = true;
        }
        else{
            toggleBtn.textContent = "파일 변경";
            reservationLoadBox.style.display = "block";
            reservationFileBox.style.display = "none";
            document.querySelector("#reservationFileBox > input").required = false;
            document.querySelector("#reservationFileBox > input").value = '';

        }
    })
}