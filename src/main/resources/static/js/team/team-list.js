let currentPage = 0;

function loadTeamPage(page) {
    const region = document.getElementById('region').value;
    const position = document.getElementById('recruiting-position').value;
    const rating = document.getElementById('rating-filter').value || 0;

    fetch(`/team/team/list?page=${page}&region=${region}&recruitingPosition=${position}&teamRatingAverage=${rating}`)
        .then(res => res.json())
        .then(data => {
            const teams = data.data.items;
            const pageInfo = data.data.pageInfo;
            const container = document.getElementById('team-container');
            const paging = document.getElementById('paging-container');

            container.innerHTML = '';
            paging.innerHTML = '';

            if (!teams || teams.length === 0) {
                container.innerHTML = '<p>등록된 팀이 없습니다.</p>';
                return;
            }

            teams.forEach(team => {
                container.innerHTML += `
          <div class="team-card" style="border: 1px solid #ccc; padding: 10px; margin-bottom: 10px;">
            <h3>${team.teamName}</h3>
            <p>지역: ${team.teamRegion}</p>
            <p>별점: ${team.teamRatingAverage} ★</p>
            <p>포지션: ${team.recruitingPositions.join(', ')}</p>
            <p>${team.recruitmentStatus ? '모집 중' : '모집 완료'}</p>
          </div>
        `;
            });

            // Previous button
            if (!pageInfo.isFirst) {
                paging.innerHTML += `<button onclick="loadTeamPage(${page - 1})">이전</button>`;
            }

// Page number buttons
            for (let i = 0; i < pageInfo.totalPages; i++) {
                paging.innerHTML += `
        <button onclick="loadTeamPage(${i})" ${i === page ? 'style="font-weight:bold;"' : ''}>
            ${i + 1}
        </button>
    `;
            }

// Next button
            if (!pageInfo.isLast) {
                paging.innerHTML += `<button onclick="loadTeamPage(${page + 1})">다음</button>`;
            }

            currentPage = page;
        });
}

document.addEventListener('DOMContentLoaded', () => {
    loadTeamPage(0);

    // Optional: filter button reloads page 0
    document.getElementById('filterBtn').addEventListener('click', () => {
        loadTeamPage(0);
    });
});