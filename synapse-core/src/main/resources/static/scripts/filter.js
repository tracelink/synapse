$(document).ready(function() {
	$('[data-toggle="configure"]').each(function() {
		let button = this;
		button.addEventListener("click", function() {
			event.preventDefault();
			let target = $('#configTarget');
			if (target.children().length > 0) {
				target.empty();
			}
			let contentwrapper = $(button).data('contentwrapper');
			let copyNode = $("div [data-configure='" + contentwrapper + "'").children("div");
			$(copyNode).clone().appendTo(target);
		});
	})
	$('#filterFilter').click();
});

$(document).ready(function() {
	$("#filterpicker").submit(function(e) {
		e.preventDefault();
		window.location.href = $("select").val()
	});
});

$(document).ready(function() {
	$('#deleteModal').on('show.bs.modal', function(e) {
		let target = $(e.relatedTarget);
		let action = target.data('action');
		let item = target.data('item');
		let name = target.data('inputname');
		let id = target.data('id');
		$("#deleteModalItem").html(item);
		$("#deleteModalName").html(id);
		$("#deleteModalForm").attr("action", action);
		$("#deleteModalInput").attr("name", name);
		$("#deleteModalInput").attr("value", id);
	});
});

$(document).ready(function() {
	$('#addProjectsModal').on('show.bs.modal', function(e) {
		let target = $(e.relatedTarget);
		let action = target.data('action');
		let id = target.data('id');
		$("#addProjectModalName").html(id);
		$("#addProjectModalForm").attr("action", action);
	}).on('shown.bs.modal', function(){
		$("#addProjectModalForm select[name='projectNames']").trigger('focus');
	});
});

$(document).ready(function() {
	$('#renameFilterModal').on('show.bs.modal', function(e) {
		let target = $(e.relatedTarget);
		let action = target.data('action');
		let id = target.data('id');
		$("#renameFilterModalName").html(id);
		$("#renameFilterModalForm").attr("action", action);
		$("#renameFilterModalForm input[name='filterName']").attr("value", id);
	}).on('shown.bs.modal', function(){
		$("#renameFilterModalForm input[name='filterName']").trigger('focus');
	});
});
