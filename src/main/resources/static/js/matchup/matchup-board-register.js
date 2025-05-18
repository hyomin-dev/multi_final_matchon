document.addEventListener("DOMContentLoaded",()=>{
    getSportsType();
    getTeam();
    setCurrentParticipants();
    setMannerTemperature();

    const cancelBtn = document.querySelector(".cancel-btn");
    const form = document.querySelector("form");
    form.addEventListener("submit",()=>{
        alert("test");
    })


    cancelBtn.addEventListener("click",()=>{
        window.history.back();
    })
})
async function getSportsType(){
    const response = await fetch("/sports-types")
    if(!response.ok)
        throw new Error(`HTTP error! Status:${response.status}`)
    const data = await response.json();
    //console.log(data); // data 확인
    //console.log(data.length); // data 길이 확인
    const selectBtn = document.querySelector(".sports");
    for(let i=0;i<data.length;i++){
        const option = document.createElement("option")
        option.value = data[i].sportsTypeName;
        option.textContent = data[i].sportsTypeName;
        selectBtn.appendChild(option);
    }
}

async function getTeam(){
    const selectBtn = document.querySelector(".team");
    const option = document.createElement("option")
    option.value = "Team1";
    option.textContent = "Team1";
    selectBtn.appendChild(option);
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

            document.getElementById("sports-facility-address").value = addr;
            // 커서를 상세주소 필드로 이동한다.
            document.getElementById("sports-facility-address").focus();
        }
    }).open();
}
function setCurrentParticipants(){
    const selectCur = document.querySelector(".current-participants");
    const selectMax = document.querySelector(".max-participants");
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
    const selectManner = document.querySelector(".manner-temperature");
    for(let i=30;i<=40;i+=0.5){
        const option = document.createElement("option");
        option.value = i;
        option.textContent = i;
        selectManner.appendChild(option);
    }
}