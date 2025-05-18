document.addEventListener("DOMContentLoaded",()=>{

    document.querySelector("#register").addEventListener("click",()=>{
        boardRegister();
    })
})

async function boardRegister(){
    alert("test");
    const token = localStorage.getItem("accessToken");
    const response = await fetch("/matchup/board/register",{
        headers: {
            Authorization: "Bearer "+token
        },
        method: "GET"
    })

    if(!response.ok)
        throw new Error(`HTTP error! Status:${response.status}`)
    const data = await response.json();
    console.log(data.data);
    window.location.href = data.data+".html";
}