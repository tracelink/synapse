const branchesTableLoad = async () => {
  $('#projectPicker').on('changed.bs.select', updateProjectView);
};

const updateProjectView = async (event) => {
  project = event.target.value;
  $('.branchesRow').hide();
  $('.branchesRow').removeClass('d-none');
  $('#branchesTable').removeClass('d-none');
  $("tr[id='" + project + "-branches']").show();
}

var project = null;

$(function () {
	branchesTableLoad();
})
