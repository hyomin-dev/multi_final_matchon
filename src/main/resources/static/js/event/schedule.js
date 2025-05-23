const userRole = "USER";
const calendar = document.getElementById("calendar");
const monthYear = document.getElementById("monthYear");
const tooltip = document.getElementById("tooltip");

let selectedCell = null;
let selectedDate = null;
let today = new Date();
let currentYear = today.getFullYear();
let currentMonth = today.getMonth();

const monthNames = ['JAN','FEB','MAR','APR','MAY','JUN','JUL','AUG','SEP','OCT','NOV','DEC'];
const weekDays = ['SUN','MON','TUE','WED','THU','FRI','SAT'];

const regionColors = {
    "CAPITAL_REGION": "#FFC3C3",
    "JEJU": "#FFC582",
    "YEONGNAM_REGION": "#FFEDA3",
    "HONAM_REGION": "#C7FAB8",
    "CHUNGCHEONG_REGION": "#B5DBFF",
    "GANGWON_REGION": "#E7D0FF"
};

const regionLabels = {
    "CAPITAL_REGION": "수도권",
    "JEJU": "제주권",
    "YEONGNAM_REGION": "영남권",
    "HONAM_REGION": "호남권",
    "CHUNGCHEONG_REGION": "충청권",
    "GANGWON_REGION": "강원권"
};



async function renderCalendar(year, month) {
    calendar.innerHTML = "";
    weekDays.forEach((day, i) => {
        const header = document.createElement("div");
        header.classList.add("day-header");
        if (i === 0) header.classList.add("sunday");
        if (i === 6) header.classList.add("saturday");
        header.textContent = day;
        calendar.appendChild(header);
    });

    const daysInMonth = new Date(year, month + 1, 0).getDate();
    const startDay = new Date(year, month, 1).getDay();

    for (let i = 0; i < startDay; i++) {
        const empty = document.createElement("div");
        empty.classList.add("day-cell");
        calendar.appendChild(empty);
    }

    const response = await fetch(`/api/events?year=${year}&month=${month + 1}`);
    const calendarData = await response.json();

    for (let day = 1; day <= daysInMonth; day++) {
        const cell = document.createElement("div");
        cell.classList.add("day-cell");

        const dateObj = new Date(year, month, day);
        const localDateStr = `${year}-${String(month + 1).padStart(2, '0')}-${String(day).padStart(2, '0')}`;

        if (dateObj.toDateString() === new Date().toDateString()) {
            cell.classList.add("today");
        }

        const dayNum = document.createElement("div");
        dayNum.classList.add("day-number");
        if (dateObj.getDay() === 0) dayNum.classList.add("sunday");
        dayNum.textContent = day;
        cell.appendChild(dayNum);

        const matched = calendarData.find(d => d.date === localDateStr);
        if (matched && matched.events.length > 0) {
            matched.events.forEach(e => {
                const label = document.createElement("div");
                label.classList.add("event-label");
                label.textContent = e.eventTitle;
                label.style.backgroundColor = regionColors[e.region] || "#bbb";

                label.addEventListener("click", () => {
                    window.location.href = `/event/${e.id}`;
                });

                label.addEventListener("mouseover", (event) => {
                    tooltip.innerText =
                        `📌 ${e.eventTitle}\n` +
                        `📅 ${localDateStr}\n` +
                        `🏢 ${e.hostName || '미정'}\n` +
                        `📍 ${e.eventAddress || '미정'}\n` +
                        `📝 ${e.eventMethod || '미정'}\n` +
                        `📞 ${e.eventContact || '없음'}`;
                    tooltip.style.display = 'block';
                });


                label.addEventListener("mousemove", (event) => {
                    tooltip.style.left = event.pageX + 10 + 'px';
                    tooltip.style.top = event.pageY + 10 + 'px';
                });
                label.addEventListener("mouseout", () => {
                    tooltip.style.display = 'none';
                });

                cell.appendChild(label);
            });
        }

        cell.addEventListener("click", () => {
            if (userRole !== "HOST") return;
            if (selectedCell) selectedCell.classList.remove("selected");
            cell.classList.add("selected");
            selectedCell = cell;
            selectedDate = localDateStr;
        });

        calendar.appendChild(cell);
    }

    monthYear.textContent = `${monthNames[month]} ${year}`;
}

function changeMonth(offset) {
    currentMonth += offset;
    if (currentMonth < 0) { currentMonth = 11; currentYear--; }
    else if (currentMonth > 11) { currentMonth = 0; currentYear++; }
    renderCalendar(currentYear, currentMonth);
}

document.addEventListener("DOMContentLoaded", () => {
    renderCalendar(currentYear, currentMonth);
});

function submitSelectedDate() {
    if (!selectedDate) return alert("날짜를 선택해주세요.");
    window.location.href = `/event/new?selectedDate=${selectedDate}`;
}