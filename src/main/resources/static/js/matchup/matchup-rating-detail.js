document.addEventListener("DOMContentLoaded",()=>{
    const detailDto = document.querySelector("#matchup-rating-detail-dto");
    const boardId = Number(detailDto.dataset.boardId);

    document.querySelector(".back-btn").addEventListener("click",()=>{
        window.location.href = `/matchup/rating/page?boardId=${boardId}`;
    })
})