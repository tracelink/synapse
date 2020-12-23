$(document).ready(function() {
	$('#copyResponseModal').on('show.bs.modal', function(e) {
		let target = $(e.relatedTarget);
		let responseId = target.data('responseid');
		let responseName = target.data('responsename');
		
		$("#copyResponseId").val(responseId);
		$("#copyResponseName").html(responseName);
	});
});