$(document).ready(function() {
	$('#editSurveyModal').on('show.bs.modal', function(e) {
		let target = $(e.relatedTarget);
		let func = target.data('function');
		let practice = target.data('practice');
		let measure = target.data('measure');
		let detail = target.data('detail');
		let measureId = target.data('measureid');
		let status = target.data('status');
		let responsible = target.data('responsible');
		let response = target.data('response');
		
		$("#editModalFunction").html(func);
		$("#editModalPractice").html(practice);
		$("#editModalMeasure").html(measure);
		$("#editModalDetail").html(detail);
		$("#editModalMeasureId").val(measureId);
		$("#editModalStatus").val(status);
		$("#editModalStatus").selectpicker("refresh");
		$("#editModalResponsible").val(responsible);
		$("#editModalResponse").val(response);
	});
});