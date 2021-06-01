$(document).ready(function() {
	// Configure tooltip
    $('[data-toggle="tooltip"]').tooltip();

    // Configure delete modal
	$('#deleteModal').on('show.bs.modal', function(e) {
		let target = $(e.relatedTarget);
		let item = target.data('item');
		let name = target.data('name');
		let action = target.data('action');
		let input = target.data('input');
		let id = target.data('id');
		$("#deleteModalItem").html(item);
		$("#deleteModalName").html(name);
		$("#deleteModalForm").attr("action", action);
		$("#deleteModalInput").attr("name", input);
		$("#deleteModalInput").attr("value", id);
	});
});
