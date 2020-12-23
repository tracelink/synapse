$(document).ready(function() {
	$('#logger_select').on('change', function(e) {
		$('#logger_form').submit();
	});
});