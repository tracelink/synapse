$(document).ready(function() {
	var activeCell;
	const partials = [ "sl", "m", "o", "s", "ed", "ee", "a",
		"id", "lc", "li", "lav", "lac", "fd", "rd",
		"nc", "pv" ];
	
	$('[data-toggle="tooltip"]').tooltip()
	$('.custom-select').change(function() {
		$(this).removeClass('text-0 text-1 text-2 text-3 text-4 text-5 text-6 text-7 text-8 text-9');
		$(this).addClass('text-' + $(this).val());
		calculate();
	});
	init(getUrlParameter('v'));
	colorize();
	calculate();
	
	
	
	function getUrlParameter(name) {
		name = name.replace(/[\[]/, '\\[').replace(/[\]]/, '\\]');
		var regex = new RegExp('[\\?&]' + name + '=([^&#]*)');
		var results = regex.exec(location.search);
		return results === null ? ''
				: decodeURIComponent(results[1].replace(/\+/g, ' '));
	}
	
	function init(v) {
		for (let i = 0; i < partials.length; i++) {
			$("#" + partials[i]).val(0);
		}
		const reg = /^\d+$/;
		if (reg.test(v) && v.length == 16) {
			for (let i = 0; i < v.length; i++) {
				$("#" + partials[i].toLowerCase()).val(parseInt(v[i]));
			}
		}
	}

	function colorize() {
		$('.custom-select').each(function() {
			$(this).removeClass('text-0 text-1 text-2 text-3 text-4 text-5 text-6 text-7 text-8 text-9');
			$(this).addClass('text-' + $(this).val());
		});
	}
	
	function getMatrixForRisk(score) {
		if (score < 2)
			return 0;
		if (score < 4)
			return 1;
		if (score < 6)
			return 2;
		if (score < 8)
			return 3;
		return 4;
	}
	
	function calculate() {
		let threatFactors = (+$("#sl").val() + +$("#m").val()
				+ +$("#o").val() + +$("#s").val()) / 4;
		let vulnFactors = (+$("#ed").val() + +$("#ee").val()
				+ +$("#a").val() + +$("#id").val()) / 4;
		let techFactors = (+$("#lc").val() + +$("#li").val()
				+ +$("#lav").val() + +$("#lac").val()) / 4;
		let busFactors = (+$("#fd").val() + +$("#rd").val()
				+ +$("#nc").val() + +$("#pv").val()) / 4;
		
		createShortScore();
		
		let likelihood = (threatFactors + vulnFactors) / 2;
		let impact = techFactors;
		if (busFactors != 0) {
			impact = busFactors;
		}
		
		$('#TAF').text(threatFactors);
		$('#VF').text(vulnFactors);
		$('#TIF').text(techFactors);
		$('#BIF').text(busFactors);
		$('#LF').text($('#riskTable tr:eq(1) th:eq('+(1+getMatrixForRisk(likelihood))+')').text());
		$('#IF').text($('#riskTable tr:eq('+(7-getMatrixForRisk(impact))+') th:eq(0)').text());
		
		setSeverity(likelihood, impact);
	}
	
	function createShortScore(){
		let scoreShort = '';
		for (var i = 0; i < partials.length; i++) {
			scoreShort = scoreShort + $("#" + partials[i]).val();
		}
		$('#scorev').text(scoreShort);
		var url = window.location.href.split('?')[0];
		$("#scorev").attr("href", url + "?v=" + scoreShort);
	}
	
	function setSeverity(likelihood, impact){
		var matrixLikelihood = getMatrixForRisk(likelihood);
		var matrixImpact = getMatrixForRisk(impact);
		let cell = $('#riskTable tr:eq(' + (7-matrixImpact) + ') td:eq(' + matrixLikelihood +')')
		activateCell(cell);
		let sevStyle = $(cell)[0].classList[0];
		let sevText = $(cell).text();
		$('#Severity')
			.removeClass('text-Note text-Low text-Med text-High text-Critical')
			.addClass(sevStyle);
		$('#Severity').text(sevText);
	}
	
	function activateCell(cell){
		if(activeCell != undefined){
			activeCell.removeClass('active-border');
		}
		cell.addClass('active-border');
		activeCell = cell;
	}
});