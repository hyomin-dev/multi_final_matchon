function getAddress() {
    new daum.Postcode({
        oncomplete: function(data) {
            var addr = '';
            if (data.userSelectedType === 'R') {
                addr = data.roadAddress;
            } else {
                addr = data.jibunAddress;
            }
            document.querySelector("#eventAddress").value = addr;
            document.querySelector("#eventAddress").focus();
        }
    }).open();
}