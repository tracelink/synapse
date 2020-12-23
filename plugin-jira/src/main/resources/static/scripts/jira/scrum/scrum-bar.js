const barContentLoad = async () => {
	updateData();

	$('#bar-period').change(updateTimePeriod);
};

const updateTimePeriod = async (event) => {
	period = event.target.value;
	updateData();
};

const updateData = async () => {
    const response = await fetch(`/jira/rest/metrics/scrum?period=${period}`);
    const result = await response.json();

	const labels = result.labels;

	// Solves bug where old data flashes on chart
	if (scrumBar != null) {
		scrumBar.destroy();
	}

	// Pull out datasets and hide chart if there is no data
	let sets = [];
	let hide = true;
	let colorIndex = 0;
	const colors = getColors(Object.keys(result).length - 1);

	for (var key in result) {
		if (key == 'labels') {
			continue;
		}
		// Add set to datasets
		let set = {
			label: key,
			borderColor: colors[colorIndex],
			backgroundColor: colors[colorIndex],
			fill: false,
			data: result[key]
		}
		sets.push(set);

		for (let i = 0; i < labels.length; i++) {
			if (result[key][i] != 0) {
				hide = false;
				break;
			}
		}
		colorIndex += 1
	}
	// Hide chart if no data
	toggleChart(hide, 'bar-no-data', 'scrum-bar');

	var data = {
		datasets: sets,
		labels: labels
	};

	var options = {
		legend: {
			position: 'top',
			labels: {
				 padding: 20,
				 boxWidth: 15
			}
		},
		cutoutPercentage: 0,
		// Uncomment the following line in order to disable the animations.
		// animation: false,
		tooltips: {
			custom: false,
			mode: 'index',
			position: 'nearest'
		},
		scales: {
			xAxes: [{ stacked: true }],
			yAxes: [{
				stacked: true,
				display: true,
				ticks: {
					beginAtZero: true
				}

			}]
		},
		layout: {
	        padding: {
	            left: 10,
	            right: 10
	        }
	    },
	    maintainAspectRatio: false
	};

	var ctx = document.getElementById('scrum-bar').getContext('2d');

	// Generate the users by device chart.
	scrumBar = new Chart(ctx, {
		type: 'bar',
		data: data,
		options: options
	});
};

var scrumBar = null;
var period = 'last-four-weeks';

$(function () {
	barContentLoad();
})
