document.addEventListener("DOMContentLoaded",()=>{

    const form = document.querySelector("form");
    form.addEventListener("submit", (event)=>{
            submitCheck(event)
        }
    )

    cancelBtn.addEventListener("click",()=>{
        window.history.back();
    })
})

function submitCheck(e){


    const mannerScoreEle = document.querySelector("#mannerScore");

    const skillScoreEle = document.querySelector("#skillScore");

    const reviewEle = document.querySelector("#review");



    if(mannerScoreEle.value ===""){
        alert("매너 점수를 입력하세요.");
        e.preventDefault();
    } else if(skillScoreEle.value ===""){
        alert("실력 점수를 입력하세요.");
        e.preventDefault();
    } else if(reviewEle.value ===""){
        alert("리뷰를 입력하세요");
        e.preventDefault();
    } else{
        alert("매너 후기가 보내졌습니다.");
    }
}