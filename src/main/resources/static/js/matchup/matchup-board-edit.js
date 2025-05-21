document.addEventListener("DOMContentLoaded",()=>{

    const editDto = document.querySelector("#matchup-board-edit-dto");

    const boardId = Number(editDto.dataset.boardId);
    const writer = editDto.dataset.writer;
    const sportsTypeName = editDto.dataset.sportsTypeName;
    const sportsFacilityName = editDto.dataset.sportsFacilityName;
    const sportsFacilityAddress = editDto.dataset.sportsFacilityAddress;
    const matchDatetime = editDto.dataset.matchDatetime;
    const matchDuration = editDto.dataset.matchDuration;
    const currentParticipantCount = Number(editDto.dataset.currentParticipantCount);
    const maxParticipants = Number(editDto.dataset.maxParticipants);
    const minMannerTemperature = Number(editDto.dataset.minMannerTemperature);
    const originalName = editDto.dataset.originalName;
    const savedName = editDto.dataset.savedName;
    const savedPath = editDto.dataset.savedPath;
    const myTemperature = Number(editDto.dataset.myTemperature);
    const loginMember = editDto.dataset.loginMember;

    getSportsType(sportsTypeName); // 종목 가져옴

    setMaxParticipants(maxParticipants);
    setMannerTemperature(minMannerTemperature);

    // const cancelBtn = document.querySelector(".cancel-btn");
    // const form = document.querySelector("form");
    // form.addEventListener("submit", (event)=>{
    //     submitCheck(event)
    // }
    // )
    //
    // cancelBtn.addEventListener("click",()=>{
    //     window.history.back();
    // })
})

function submitCheck(e){

    alert("submit")
    document.querySelector("#teamName").disabled=false;


    //e.preventDefault();
}

async function getSportsType(sportsTypeName){
    const response = await fetch("/sports-types",{
        method: "GET",
        credentials: "include"
    })
    if(!response.ok)
        throw new Error(`HTTP error! Status:${response.status}`)
    const data = await response.json();
    //console.log(data); // data 확인
    //console.log(data.length); // data 길이 확인

    const selectBtn = document.querySelector("#sportsTypeName");

    for(let i=0;i<data.length;i++){
        const option = document.createElement("option")
        option.value = data[i].sportsTypeName;
        option.textContent = data[i].sportsTypeName;

        if(data[i].sportsTypeName===sportsTypeName)
            option.selected = true;
        selectBtn.appendChild(option);
    }
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


function setMaxParticipants(maxParticipants){

    const selectMax = document.querySelector("#maxParticipants");

    for(let i=1; i<=30;i++){
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