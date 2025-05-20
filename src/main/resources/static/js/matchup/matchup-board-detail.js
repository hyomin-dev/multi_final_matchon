document.addEventListener("DOMContentLoaded",()=>{
        setContent();
})


async function setContent(){
    const detailDto = document.querySelector("#matchup-board-detail-dto");

    const boardId = detailDto.dataset.boardId;
    const writer = detailDto.dataset.writer;
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




    const response = await fetch(`${baseUrl}${savedName}`,{
        method: "GET",
        credentials: "include"
        })
    if(!response.ok)
        throw new Error(`HTTP error! Status:${response.status}`)
    const data = await response.json();
    console.log(data);
}
