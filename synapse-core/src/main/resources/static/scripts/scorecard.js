$(document).ready(function() {
	$('#filterpicker').on('change', function(e) {
		let picker = $(this).find("option:selected").data('id');
		$('#pickerforms > form').hide();
		$('#' + picker).show();
	});
	$('#pickerforms > form').hide();
});