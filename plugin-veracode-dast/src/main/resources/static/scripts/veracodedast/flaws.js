const issuesTableLoad = async () => {
    $("table").DataTable();
    $('.issuesTable').hide();
    $('.issuesTable').removeClass('d-none');
    $('#productLinePicker').on('changed.bs.select', updateProductLineView);
};

const updateProductLineView = async (event) => {
	productLine = event.target.value;
	$('.issuesTable').hide();
	$("div[id='issuesTable-" + productLine + "']").show();
}

var productLine = null;

$(function () {
	issuesTableLoad();
})
