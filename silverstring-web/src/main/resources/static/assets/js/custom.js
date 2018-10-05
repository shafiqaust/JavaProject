'use strict';

//=============================
// Page transition fade effect
//=============================
$(window).unload(function () {
  document.body.classList.add("animate-out");
});

//=============================
//Collapse
//=============================
$('div.collapse').collapse('hide');

//=============================
// Countimator
//=============================
$("span.counter").countimator();

//=============================
// Plyr js
//=============================
plyr.setup();

//=============================
// Charts JS
//=============================
var ctx = $("#btc-chart");
if(ctx.length) var myChart = new Chart(ctx, {
	type: 'line',
	responsive: 'true',
	data: {
		labels: ["2000", "2010", "2015", "2020", "2025", "2030"],
		datasets: [{
			label: '# of Votes',
			data: [12, 19, 3, 5, 2, 3],
			backgroundColor: [
			   'rgba(0, 0, 0, 0.1)'
			],
			borderColor: [
				'rgba(49, 85, 246, 1)'
			],
			borderWidth: 3
		}],
	},
	options: {
		scales: {
			yAxes: [{
				ticks: {
					beginAtZero:true
				}
			}]
		}
	}
});
var ctx = $("#btcc-chart");
if(ctx.length) var myChart = new Chart(ctx, {
	type: 'line',
	responsive: 'true',
	data: {
		labels: ["2000", "2010", "2015", "2020", "2025", "2030"],
		datasets: [{
			label: '# of Votes',
			data: [2, 5, 15, 2, 2, 3],
			backgroundColor: [
			   'rgba(0, 0, 0, 0.1)'
			],
			borderColor: [
				'rgba(49, 85, 246, 1)'
			],
			borderWidth: 3
		}],
	},
	options: {
		scales: {
			yAxes: [{
				ticks: {
					beginAtZero:true
				}
			}]
		}
	}
});
var ctx = $("#ltc-chart");
if(ctx.length) var myChart = new Chart(ctx, {
	type: 'line',
	responsive: 'true',
	data: {
		labels: ["2000", "2010", "2015", "2020", "2025", "2030"],
		datasets: [{
			label: '# of Votes',
			data: [2, 3, 4, 5, 6, 15],
			backgroundColor: [
			   'rgba(0, 0, 0, 0.1)'
			],
			borderColor: [
				'rgba(49, 85, 246, 1)'
			],
			borderWidth: 3
		}],
	},
	options: {
		scales: {
			yAxes: [{
				ticks: {
					beginAtZero:true
				}
			}]
		}
	}
});
var ctx = $("#eth-chart");
if(ctx.length) var myChart = new Chart(ctx, {
	type: 'line',
	responsive: 'true',
	data: {
		labels: ["2000", "2010", "2015", "2020", "2025", "2030"],
		datasets: [{
			label: '# of Votes',
			data: [2, 3, 5, 10, 15, 20],
			backgroundColor: [
			   'rgba(0, 0, 0, 0.1)'
			],
			borderColor: [
				'rgba(49, 85, 246, 1)'
			],
			borderWidth: 3
		}],
	},
	options: {
		scales: {
			yAxes: [{
				ticks: {
					beginAtZero:true
				}
			}]
		}
	}
});
var ctx = $("#xrp-chart");
if(ctx.length) var myChart = new Chart(ctx, {
	type: 'line',
	responsive: 'true',
	data: {
		labels: ["2000", "2010", "2015", "2020", "2025", "2030"],
		datasets: [{
			label: '# of Votes',
			data: [20, 18, 15, 10, 9, 15],
			backgroundColor: [
			   'rgba(0, 0, 0, 0.1)'
			],
			borderColor: [
				'rgba(49, 85, 246, 1)'
			],
			borderWidth: 3
		}],
	},
	options: {
		scales: {
			yAxes: [{
				ticks: {
					beginAtZero:true
				}
			}]
		}
	}
});

//=============================
// Tooltip
//=============================
$('[data-toggle="tooltip"]').tooltip();

//=============================
// Owl slider
//=============================
// clients
var owl = $('#clients-slider');
owl.owlCarousel({
	autoplay: true,
	autoplayTimeout:5000,
	loop: true,
	items: 3,
	margin: 30,
	dots: true,
	nav: false,
	autoplayHoverPause: true,
	responsiveClass:true,
    responsive:{
        0:{
            items:1,
        },
        576:{
            items:1,
        },
		768:{
            items:3,
        }
    }
});
var owl2 = $('#statics-slider');
owl2.owlCarousel({
	autoplay: true,
    autoplayTimeout: 0,
    autoplayHoverPause: true,
    autoplaySpeed: 3000,
    navRewind: false,
	loop: true,
	items: 6,
	margin: 30,
	dots: true,
	nav: false,
	responsiveClass:true,
    responsive:{
        0:{
            items:1,
        },
        576:{
            items:3,
        },
		768:{
            items:6,
        }
    }
});
var owl3 = $('#statics-slider-2');
owl3.owlCarousel({
	autoplay: true,
    autoplayTimeout: 0,
    autoplayHoverPause: true,
    autoplaySpeed: 3000,
    navRewind: false,
	loop: true,
	items: 5,
	margin: 30,
	dots: false,
	nav: false,
	responsiveClass:true,
    responsive:{
        0:{
            items:1,
        },
		500:{
            items:2,
        },
		900:{
			items:3,
		},
        1000:{
            items:4,
        },
		1200:{
			items:5,
		}
    }
});
var owl4 = $('#team-slider');
owl4.owlCarousel({
	autoplay: true,
	autoplayTimeout:5000,
	loop: true,
	items: 3,
	margin: 30,
	dots: true,
	nav: false,
	autoplayHoverPause: true,
	responsiveClass:true,
    responsive:{
        0:{
            items:1,
        },
        576:{
            items:1,
        },
		768:{
            items:3,
        }
    }
});

//=============================
// Retina
//=============================
retinajs();

//=============================
// Close menu when click outside
//=============================
$(document).bind('click', function (e) {
	var clickover = $(e.target);
	var _opened = $(".navbar-collapse").hasClass("navbar-collapse collapse show");
	if (_opened === true && !clickover.hasClass("navbar-toggler")) {
		$("button.navbar-toggler").click();
	}
});

//=============================
// Scroll to top
//=============================
$("#back-top").hide();
$(function () {
	$(window).scroll(function () {
		if ($(this).scrollTop() > 100) {
			$('#back-top').fadeIn();
		} else {
			$('#back-top').fadeOut();
		}
	});
	// scroll body to 0px on click
	$('#back-top a').bind('click', function (e) {
		$('body,html').animate({
			scrollTop: 0
		}, 800);
		return false;
	});
});
