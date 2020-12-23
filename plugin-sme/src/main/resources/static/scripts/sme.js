$(document).ready(function() {
	$('#setProjectsModal').on('show.bs.modal', function(e) {
		let target = $(e.relatedTarget);
		let sme = target.data('sme');
		let projectsList = target.data('projects');
		if(projectsList){
			let projectsArr = projectsList.split(',');
			$("#setProjectsSelectPicker").selectpicker('val', projectsArr);
		}
		$("#setProjectsModalName").html(sme);
		$("#setProjectsSmeName").attr("value", sme);
		
	});
});

$(document).ready(function() {
	$('#removeProjectModal').on('show.bs.modal', function(e) {
		let target = $(e.relatedTarget);
		let sme = target.data('sme');
		let project = target.data('project');
		$("#removeProjectModalName").html(sme);
		$("#removeProjectModalProject").html(project);
		$("#removeProjectSmeName").attr("value", sme);
		$("#removeProjectName").attr("value", project);
	});
});