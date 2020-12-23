const barContentLoad = async () => {
	updateData();

	$('#view-by').change(updateViewBy);
	$('#productLine').change(updateSelection);
	$('#bar-period').change(updateTimePeriod);
	$('#bar-category').change(updateCategory);

	$('#productLine').hide();
};

const updateViewBy = async (event) => {
    viewBy = event.target.value;

    $('#productLine').hide();

    $("#" + viewBy).show();
    selection = $("#" + viewBy + " option:first").val();

    updateData();
};

const updateSelection = async (event) => {
	selection = event.target.value
	updateData();
};

const updateTimePeriod = async (event) => {
	period = event.target.value;
	updateData();
};

const updateCategory = async (event) => {
	category = event.target.value;
	updateData();
}

const updateData = async () => {
	let response;
	if(viewBy == 'all'){
		response = await fetch(`/veracodedast/rest/flaws?period=${period}&category=${category}`);    
    }else{
    	response = await fetch(`/veracodedast/rest/flaws?${viewBy}=${selection}&period=${period}&category=${category}`);
    }
    const result = await response.json();

	const labels = result.labels;

	// Solves bug where old data flashes on chart
	if (viosBar != null) {
		viosBar.destroy();
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
	toggleChart(hide, 'bar-no-data', 'violations-bar');

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

	var ctx = document.getElementById('violations-bar').getContext('2d');

	// Generate the users by device chart.
	viosBar = new Chart(ctx, {
		type: 'bar',
		data: data,
		options: options
	});
};

var viosBar = null;
var period = 'last-four-weeks';
var viewBy = 'all';
var category = 'severity'
var selection = null;

$(function () {
	barContentLoad();
})
