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
	$('#editProductLine').click();
});

$(document).ready(function() {
	$("#productlinepicker").submit(function(e) {
		e.preventDefault();
		window.location.href = $("select").val()
	});
});

$(document).ready(function() {
	$('#createModal').on('show.bs.modal', function(e) {
		let target = $(e.relatedTarget);
		let action = target.data('action');
		$("#createModalForm").attr("action", action);
	}).on('shown.bs.modal', function(){
		$("#createModalForm input[name='projectName']").trigger('focus');

	});
	
});

$(document).ready(function() {
	$('#renameProjectModal').on('show.bs.modal', function(e) {
		let target = $(e.relatedTarget);
		let id = target.data('id');
		let action = target.data('action');
		$("#renameProjectModalForm").attr("action", action);
		$("#oldProjectName").attr("value", id);
		$("#renameProjectModalForm input[name='projectName']").attr("value", id)
	}).on('shown.bs.modal', function(){
		$("#renameProjectModalForm input[name='projectName']").trigger('focus');
	});
});

$(document).ready(function() {
	$('#renameProductLineModal').on('show.bs.modal', function(e) {
		let target = $(e.relatedTarget);
		let action = target.data('action');
		let id = target.data("id");
		$("#renameProductLineModalForm").attr("action", action);
		$("#renameProductLineModalForm input[name='productLineName']").attr("value", id);
	}).on('shown.bs.modal', function(){
		$("#renameProductLineModalForm input[name='productLineName']").trigger('focus');
	});
});

$(document).ready(function() {
	$('#moveProjectModal').on('show.bs.modal', function(e) {
		let target = $(e.relatedTarget);
		let action = target.data('action');
		let id = target.data('id');
		$("#moveProjectModalName").html(id);
		$("#moveProjectModalForm").attr("action", action);
		$("#moveProjectModalInput").attr("value", id);
	}).on('shown.bs.modal', function(){
		$("#moveProjectModalForm select[name='productLineName']").trigger('focus');
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

