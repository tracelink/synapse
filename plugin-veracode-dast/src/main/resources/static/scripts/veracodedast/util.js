const toggleChart = (hide, none, canvas) => {
	if (hide) {
		document.getElementById(none).innerHTML = 'No violations to display.';
		document.getElementById(canvas).style.display = 'none';
	} else {
		document.getElementById(none).innerHTML = '';
		document.getElementById(canvas).style.display = 'block';
	}
};

const getColors = (numColors) => {
	// Note that the colors will wrap after (baseColors.length) * 6 distinct rules
	let baseColors = [
        'rgba(0,86,63,t)',
        'rgba(0,167,181,t)',
        'rgba(236,232,26,t)',
        'rgba(0,61,76,t)',
        'rgba(91,103,112,t)',
        'rgba(0,135,85,t)',
        'rgba(0,85,135,t)',
	];

	let transparencies = [1, 0.85, 0.7, 0.55, 0.4, 0.25];
	let graphColors = [];

	let i = 0;
	let j = 0;

	while (graphColors.length < numColors) {
		graphColors.push(baseColors[j].replace('t', transparencies[i]));

		// If we have gone through all the colors of this transparency, restart base
		// colors and go to next transparency
		if (j == baseColors.length - 1) {
			j = 0;
			i += 1;
		} else {
			// Just increment to next base color at this transparency
			j += 1;
		}

		// If we run out of colors, wrap back to the beginning
		if (i == transparencies.length) {
			i = 0;
		}
	}
	return graphColors;
};