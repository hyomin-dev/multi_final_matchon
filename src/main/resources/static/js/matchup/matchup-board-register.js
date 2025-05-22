let myMannerTemperature;

document.addEventListener("DOMContentLoaded",async ()=>{
    getSportsType(); // 종목 가져옴
    getTeam(); // 현재 사용자의 팀 정보를 가져옴
    setCurrentParticipants();
    setMannerTemperature();

    const cancelBtn = document.querySelector(".cancel-btn");
    const form = document.querySelector("form");
    form.addEventListener("submit", (event)=>{
        submitCheck(event)
    }
    )


    cancelBtn.addEventListener("click",()=>{
        window.history.back();
    })

    myMannerTemperature = await getMyMannerTemperature();
})

function submitCheck(e){


    //document.querySelector("#teamName").disabled=false;

    const matchDateTimeEle = document.querySelector("#matchDateTime");
    //console.log(matchDateTimeEle.value);

    const currentParticipantsCountEle = document.querySelector("#currentParticipantsCount");
    //console.log(currentParticipantsCountEle.value);

    const maxParticipantsEle = document.querySelector("#maxParticipants");
    //console.log(maxParticipantsEle.value);

    const minMannerTemperatureEle = document.querySelector("#minMannerTemperature");
    //console.log(minMannerTemperatureEle.value);

    //console.log(myMannerTemperature);


    const matchDate = new Date(matchDateTimeEle.value);
    const now = new Date();
    if(matchDate<now){
        alert(`경기 시작 시간은 현재 시간(${now})이후만 가능합니다. 다시 작성해주세요.`)
        e.preventDefault();
    }else if(Number(currentParticipantsCountEle.value) >Number(maxParticipantsEle.value)){
        alert(`현재 참가 인원은 총 모집 인원보다 적어야 합니다.`)
        e.preventDefault();
    }else if(Number(minMannerTemperatureEle.value) > Number(myMannerTemperature)){
        alert(`하한 매너 온도는 작성자의 매너온도(${myMannerTemperature})이하로 지정해주세요.`)
        e.preventDefault();
    }else
        alert("submit")
}

async function getSportsType(){
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
        selectBtn.appendChild(option);
    }
}


async function getTeam(){
    const email = document.querySelector("#user-info").dataset.email;
    const team = document.querySelector("#teamName");
    const response = await fetch(`/member/search-with-team-teamname?email=${email}`,{
        method: "GET",
        credentials: "include"
    });
    if(!response.ok){
        //throw new Error(`HTTP error! Status:${response.status}`)
        alert("MATCHUP 글 작성은 소속팀이 있어야 합니다.")
        window.history.back();
    }

    const data = await response.json();
    // if(data.data.trim()==='' || data.data===null){
    //
    // }
    team.value=data.data;
    team.textContent = data.data;

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

function setCurrentParticipants(){

    const selectCur = document.querySelector("#currentParticipantsCount");
    const selectMax = document.querySelector("#maxParticipants");

    for(let i=1; i<=30;i++){
        const option1 = document.createElement("option");
        const option2 = document.createElement("option");
        option1.value = i;
        option1.textContent = i;
        selectCur.appendChild(option1);
        option2.value = i;
        option2.textContent = i;
        selectMax.appendChild(option2);
    }
}

function setMannerTemperature(){
    const selectManner = document.querySelector("#minMannerTemperature");

    for(let i=30;i<=40;i+=0.5){
        const option = document.createElement("option");
        option.value = i;
        option.textContent = i;
        selectManner.appendChild(option);
    }
}

async function getMyMannerTemperature(){

    const email = document.querySelector("#user-info").dataset.email;
    const response  = await fetch(`/member/search-temperature?email=${email}`,{
        method: "GET",
        credentials: "include"
    })
    if(!response.ok)
        throw new Error(`HTTP error! Status:${response.status}`)
    const data = await response.json();

    return data.data;



}