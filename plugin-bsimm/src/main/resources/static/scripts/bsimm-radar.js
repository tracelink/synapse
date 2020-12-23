const updateData = async (event) => {
	const selections = $('#response').val().toString();
	const comparisons = $('#comparison').val().toString();
	
	const url = `/bsimm/rest/response?responses=${selections}&comparisons=${comparisons}`
    const response = await fetch(url);
    const result = await response.json();
    
    if(radarGraph != null){
    	radarGraph.destroy();
    }
    
    let labels = [];
    let datasets = [];

    let labelSearch = result.responses;
    if(result.responses.length == 0){
	    labelSearch = result.comparisons;
    }
    
    for(func of labelSearch[0].functions){
	    for(practice of func.practices){
	    	labels.push(practice.practiceName);
	    }
    }
    
    colorIndex = 0;
    for(surveyresponse of result.responses){
    	datasets.push(makeDataSetForResponse(surveyresponse, labels, surveyresponse.title, getColor(colorIndex++)));
    }
    for(compare of result.comparisons){
        datasets.push(makeDataSetForComparison(compare, labels, compare.comparisonTitle, getColor(colorIndex++)));
    }
    
	var ctx = document.getElementById('radar-graph').getContext('2d');
    
	var data = {
		datasets: datasets,
		labels: labels
	};

	var options = {
		legend: {
			position: 'right',
		},
		scale: {
			ticks: {
				min: 0,
				max: 3,
				stepSize: 0.5
			}
		},
		layout: {
	        padding: {
	            top: 20,
	            bottom: 20
	        }
	    }
	};
	
    radarGraph = new Chart(ctx, {
		type: 'radar',
		data: data,
		options: options
	});
}; 

function makeDataSetForComparison(compare, labels, title, color){
	data = [];
	
    for (func of compare.functions){
    	for (label of labels){
    		for(practice of func.practices){
    			if(practice.practiceName == label){
		    		data.push(Math.round(practice.comparisonScore*100)/100);
		    		break;
    			}
			}
    	}
    }
    dataset={}
    dataset.label = title
    dataset.data = data;
    dataset.backgroundColor = 'rgba(0,0,0,0.0)';
    dataset.borderColor = color;
    dataset.pointBackgroundColor = color;
    dataset.borderDash = [3,2];
	return dataset;
}

function makeDataSetForResponse(result, labels, title, color){
	data = [];
	
    for (func of result.functions){
    	for (label of labels){
    		for(practice of func.practices){
    			if(practice.practiceName == label){
		    		let practiceScore = 0;
		    		for (level of practice.levels){
		    			let levelScore = 0;
			    		let measureNum = 0;
		    			for(measure of level.measures){
		    				measureNum++;
		    				levelScore += measure.measureScore;
		    			}
		    			console.log(practice.practiceName + " " + level.levelNum + " " + (levelScore/measureNum))
		    			practiceScore += levelScore/measureNum;
		    		}
		    		data.push(Math.round(practiceScore*100)/100);
		    		break;
    			}
			}
    	}
    }
    dataset={}
    dataset.label = title
    dataset.data = data;
    dataset.borderColor = color;
    dataset.pointBackgroundColor = color;
    dataset.borderWidth = '5';
    dataset.fill = true;
	return dataset;
}

const getColor = (colorIndex) => {
	if(colorChoice == null){
		// Note that the colors will wrap after (baseColors.length) * 3
		let baseColors = [
	        'rgba(0,86,63,t)',
	        'rgba(0,167,181,t)',
	        'rgba(236,232,26,t)',
	        'rgba(0,61,76,t)',
	        'rgba(91,103,112,t)',
	        'rgba(0,135,85,t)',
	        'rgba(0,85,135,t)',
		];
	
		let transparencies = [1.0, 0.8, 0.6];
		colorChoice = []
		for(transparency of transparencies){
			for(color of baseColors){
				colorChoice.push(color.replace('t', transparency));
			}
		}
	}
	return colorChoice[colorIndex % colorChoice.length];
};


var radarGraph = null;
var colorChoice = null;

$(function () {
	$('#showchart').click(updateData);
})