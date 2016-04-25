var markers = {};
var updates = {};
var pois = {};
var plans = {};
var tails = {};
var map, marker;
var plotlayers = [];
var selectedMarker;
var originLat, originLng, originZoom, currentLat, currentLng, currentZoom;

originLat=41.185356;
originLng=-8.704898;
originZoom=13;

// Sletvik Field Station NTNU :Lat: 63.59150018435369 Lng:9.53839123249054

var isMobile = {
	Android : function() {
		return navigator.userAgent.match(/Android/i);
	},
	BlackBerry : function() {
		return navigator.userAgent.match(/BlackBerry/i);
	},
	iOS : function() {
		return navigator.userAgent.match(/iPhone|iPad|iPod/i);
	},
	Opera : function() {
		return navigator.userAgent.match(/Opera Mini/i);
	},
	Windows : function() {
		return navigator.userAgent.match(/IEMobile/i)
				|| navigator.userAgent.match(/WPDesktop/i);
	},
	any : function() {
		return (isMobile.Android() || isMobile.BlackBerry() || isMobile.iOS()
				|| isMobile.Opera() || isMobile.Windows());
	}
};

$.ajaxSetup({
	cache : false
});
loadPoi();

function loadPoi() {
	var poi_json = $.getJSON("/poi", function(data) {
		removeMarkers();
		$.each(data, function(i, item) {

			pois[item.description] = item;

			var record = {
				"author" : item.author,
				"description" : item.description,
				"coordinates" : [ item.coordinates[0], item.coordinates[1] ]
			};
			marker = new L.marker([ item.coordinates[0], item.coordinates[1] ],
					{
						title : item.description,
						contextmenu : true,
						contextmenuItems : [ {
							text : 'Edit Marker',
							icon : 'images/edit.png',
							callback : editMarker,
							index : 0
						}, {
							separator : true,
							index : 1
						} ]
					});
			map.addLayer(marker);
			marker.bindPopup(item.description);
			marker.on('mouseover', function(e) {
				this.openPopup();
			});
			marker.on('mouseout', function(e) {
				this.closePopup();
			});
			marker.on('contextmenu', function(e) {
				// console.log('contextmenu: '+e.target.options.title);
				selectedMarker = e.target.options.title;
			});
			plotlayers.push(marker);
			record = null;
		});
	});
};

function removeMarkers() {
	for (i = 0; i < plotlayers.length; i++) {
		map.removeLayer(plotlayers[i]);
	}
	plotlayers = [];
	pois = {};
}

setInterval(function() {
	removeMarkers();
	loadPoi();
}, 60000);

function showCoordinates(e) {
	//alert(e.latlng);
	var mCoords = new L.marker(e.latlng).bindPopup("Lat: "+e.latlng.lat+" Lng:"+e.latlng.lng).addTo(map).openPopup();
}

function centerMap(e) {
	map.panTo(e.latlng);
	updateCookiePos();
	console.log();
	console.log("cookie data -(centerMap) - Lat: "+$.cookie('savedLat')+" Lng: "+$.cookie('savedLng')+" Zoom: "+$.cookie('savedZoom'));
	alert("The current Map coordinates are stored.");
}

function zoomIn(e) {
	map.zoomIn();
	//updateCookiePos();
}

function zoomOut(e) {
	map.zoomOut();
	//updateCookiePos();
}

function getCookiePos(){
	$.cookie('savedLat', map.getCenter().lat, { expires: 1 });
	$.cookie('savedLng', map.getCenter().lng, { expires: 1 });
	$.cookie('savedZoom', map.getZoom(), { expires: 1 });
}

function clearCookiePos(){
	$.removeCookie('savedLat');
	$.removeCookie('savedLng');
	$.removeCookie('savedZoom');
}
function updateCookiePos(){
	clearCookiePos();
	getCookiePos();
}

function editMarker(e) {
	console.log("props for marker: " + JSON.stringify(pois[selectedMarker]));
	var lat = pois[selectedMarker].coordinates[0], lng = pois[selectedMarker].coordinates[1];
	$
			.msgBox({
				type : "prompt",
				title : "Update Point of Interest",
				inputs : [ {
					header : "Description:",
					type : "text",
					name : "description_text",
					value : pois[selectedMarker].description
				} ],
				buttons : [ {
					value : "Ok"
				}, {
					value : "Cancel"
				} ],
				success : function(result, values) {
					if (result == "Ok") {
						// check if the form inputs are empty on submit
						if ($('input[name=description_text]').val() != '') {
							var val = {
								"author" : $("#log_user").text(),
								"description" : $(
										'input[name=description_text]').val(),
								"coordinates" : [ lat, lng ]
							};
							$
									.ajax({
										url : '/poi',
										dataType : 'json',
										type : 'POST',
										method : 'POST',
										contentType : 'application/json',
										async : true,
										crossDomain : true,
										data : JSON.stringify(val),
										cache : false,
										success : function(data, textStatus,
												jQxhr) {
											// console.log(data);
											pois[data.description] = data;

											console.log("POI\n:"
													+ JSON.stringify(data));
											// alert("Point of interest
											// inserted.");
											marker = new L.marker(
													[ lat, lng ],
													{
														title : data.description,
														contextmenu : true,
														contextmenuItems : [
																{
																	text : 'Edit Marker',
																	icon : 'images/edit.png',
																	callback : editMarker,
																	index : 0
																},
																{
																	separator : true,
																	index : 1
																} ]
													})
													.bindPopup($(
															'input[name=description_text]')
															.val());
											map.addLayer(marker);
											marker.on('mouseover', function(e) {
												this.openPopup();
											});
											marker.on('mouseout', function(e) {
												this.closePopup();
											});
											marker
													.on(
															'contextmenu',
															function(e) {
																// console.log('contextmenu:
																// '+e.target.options.title);
																selectedMarker = e.target.options.title;
															});
											plotlayers.push(marker);
											val = null;
										},
										error : function(jqXhr, textStatus,
												errorThrown) {
											console.log(errorThrown);
										}
									});
						} else {
							alert("empty field.");
						}
					}
					// alert(v);
				}
			});
}

function addMarker(e) {
	var lat = e.latlng.lat;
	var lng = e.latlng.lng;
	$
			.msgBox({
				type : "prompt",
				title : "Insert Point of Interest",
				inputs : [ {
					header : "Description:",
					type : "text",
					name : "description_text"
				} ],
				buttons : [ {
					value : "Ok"
				}, {
					value : "Cancel"
				} ],
				success : function(result, values) {
					if (result == "Ok") {
						// check if the form inputs are empty on submit
						if ($('input[name=description_text]').val() != '') {
							var val = {
								"author" : $("#log_user").text(),
								"description" : $(
										'input[name=description_text]').val(),
								"coordinates" : [ lat, lng ]
							};
							$
									.ajax({
										url : '/poi',
										dataType : 'json',
										type : 'POST',
										method : 'POST',
										contentType : 'application/json',
										async : true,
										crossDomain : true,
										data : JSON.stringify(val),
										cache : false,
										success : function(data, textStatus,
												jQxhr) {
											// console.log(data);
											pois[data.description] = data;

											console.log("POI\n:"
													+ JSON.stringify(data));

											// alert("Point of interest
											// inserted.");
											marker = new L.marker(
													[ lat, lng ],
													{
														title : data.description,
														contextmenu : true,
														contextmenuItems : [
																{
																	text : 'Edit Marker',
																	icon : 'images/edit.png',
																	callback : editMarker,
																	index : 0
																},
																{
																	separator : true,
																	index : 1
																} ]
													})
													.bindPopup($(
															'input[name=description_text]')
															.val());
											map.addLayer(marker);
											marker.on('mouseover', function(e) {
												this.openPopup();
											});
											marker.on('mouseout', function(e) {
												this.closePopup();
											});
											marker
													.on(
															'contextmenu',
															function(e) {
																// console.log('contextmenu:
																// '+e.target.options.title);
																selectedMarker = e.target.options.title;
															});
											plotlayers.push(marker);
											val = null;
										},
										error : function(jqXhr, textStatus,
												errorThrown) {
											console.log(errorThrown);
										}
									});
						} else {
							alert("empty field.");
						}
					}
					// alert(v);
				}
			});
}

var hybrid = L
		.tileLayer(
				'http://server.arcgisonline.com/ArcGIS/rest/services/NatGeo_World_Map/MapServer/tile/{z}/{y}/{x}',
				{
					maxZoom : 16,
					attribution : 'Map data &copy; Esri &mdash; National Geographic, Esri, DeLorme, NAVTEQ, UNEP-WCMC, USGS, NASA, ESA, METI, NRCAN, GEBCO, NOAA, iPC',
					id : 'examples.map-i875mjb7'
				});

var streets = L
		.tileLayer(
				'http://{s}.tiles.wmflabs.org/bw-mapnik/{z}/{x}/{y}.png',
				{
					maxZoom : 18,
					attribution : 'Map data &copy; <a href="http://openstreetmap.org">OpenStreetMap</a>',
					id : 'examples.map-20v6611k'
				});

var Esri_OceanBasemap = L
		.tileLayer(
				'http://server.arcgisonline.com/ArcGIS/rest/services/Ocean_Basemap/MapServer/tile/{z}/{y}/{x}',
				{
					attribution : 'Tiles &copy; ESRI',
					maxZoom : 13
				});

var Esri_WorldImagery = L
		.tileLayer(
				'http://server.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile/{z}/{y}/{x}',
				{
					attribution : 'Tiles &copy; ESRI'
				});

var osmLayer = new L.TileLayer(
		'http://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png',
		{
			maxZoom : 23,
			attribution : 'Map data &copy; OpenStreetMap contributors, CC-BY-SA'
		});

if(currentLat==undefined && currentLng==undefined && currentZoom==undefined)
{
	currentLat=$.cookie('savedLat');
	currentLng=$.cookie('savedLng');
	currentZoom=$.cookie('savedZoom');
	
	create_map(currentLat,currentLng,currentZoom);
	
}else{
	create_map(originLat,originLng,originZoom);
}


console.log("Origin data - Lat: "+originLat+" Lng: "+originLng+" Zoom: "+originZoom);
console.log("Current data - Lat: "+currentLat+" Lng: "+currentLng+" Zoom: "+currentZoom);
console.log("cookie data - Lat: "+$.cookie('savedLat')+" Lng: "+$.cookie('savedLng')+" Zoom: "+$.cookie('savedZoom'));


var kmlLayer = new L.KML("/kml/file.kmz", {
	async : true
});

map.addLayer(kmlLayer);

var SysIcon = L.Icon.extend({
	options : {
		shadowUrl : 'icons/shadow.png',
		iconSize : [ 22, 32 ],
		shadowSize : [ 24, 24 ],
		iconAnchor : [ 11, 32 ],
		shadowAnchor : [ 8, 24 ],
		popupAnchor : [ 0, -32 ]
	}
});

var cloudsLayer = L.tileLayer(
		'http://{s}.tile.openweathermap.org/map/clouds/{z}/{x}/{y}.png', {
			attribution : 'Map data &copy; OpenWeatherMap',
			maxZoom : 23,
			opacity : 0.7,
			maxNativeZoom : 18
		});

var windLayer = L.tileLayer(
		'http://{s}.tile.openweathermap.org/map/wind/{z}/{x}/{y}.png', {
			attribution : 'Map data &copy; OpenWeatherMap',
			maxZoom : 23,
			opacity : 0.7,
			maxNativeZoom : 18
		});

var precipLayer = L.tileLayer(
		'http://{s}.tile.openweathermap.org/map/precipitation/{z}/{x}/{y}.png',
		{
			attribution : 'Map data &copy; OpenWeatherMap',
			maxZoom : 23,
			opacity : 0.7,
			maxNativeZoom : 18
		});

var baseLayers = {
	"Open Street Map" : osmLayer,
	"Grayscale" : streets,
	"Terrain" : hybrid,
	"ESRI Ocean" : Esri_OceanBasemap,
	"ESRI Aerial" : Esri_WorldImagery
};

var overlays = {
	"Cloud cover" : cloudsLayer,
	"Wind speed" : windLayer,
	"Precipitation" : precipLayer
}

function create_map(lat,lng,zoom){
	
	console.log("create_map - lat: "+lat+" lng: "+lng+" zoom: "+zoom);
	if(lat==undefined && lng==undefined && zoom==undefined)
	{
		lat=originLat;
		lng=originLng;
		zoom=originZoom;
	}
	
	map = L.map('map', {
		center: [lat,lng],
		zoom: zoom,
		layers : [ osmLayer ],
		contextmenu : true,
		contextmenuWidth : 140,
		contextmenuItems : [ {
			text : 'Add Marker',
			icon : 'images/add_pin.png',
			callback : addMarker,
			index : 0
		}, {
			separator : true,
			index : 1
		}, {
			text : 'Show coordinates',
			callback : showCoordinates
		}, {
			text : 'Center map here',
			callback : centerMap
		}, '-', {
			text : 'Zoom in',
			icon : 'images/zoom-in.png',
			callback : zoomIn
		}, {
			text : 'Zoom out',
			icon : 'images/zoom-out.png',
			callback : zoomOut
		} ]
	});
}

if (isMobile.any()) {
	//alert('Mobile');
	L.control.layers(baseLayers, overlays).addTo(map);
} else {
	//alert('PC');
	L.control.weather().addTo(map);
	L.control.layers.minimap(baseLayers, overlays).addTo(map);
	var mouse_coordinates = new L.control.coordinates({
		position:"topleft",
		labelTemplateLat:"Lat: {y}",
		labelTemplateLng:"Lng: {x}",
		useLatLngOrder:true
	});
	map.addControl(mouse_coordinates);
}

var osmGeocoder = new L.Control.OSMGeocoder({
    collapsed: false,
    position: 'topleft',
    text: 'Locate',
	});

map.addControl(osmGeocoder);

var argosIcon = new SysIcon({
	iconUrl : 'icons/ico_argos.png'
});
var uavIcon = new SysIcon({
	iconUrl : 'icons/ico_uav.png'
});
var auvIcon = new SysIcon({
	iconUrl : 'icons/ico_auv.png'
});
var unknownIcon = new SysIcon({
	iconUrl : 'icons/ico_unknown.png'
});
var ccuIcon = new SysIcon({
	iconUrl : 'icons/ico_ccu.png'
});
var spotIcon = new SysIcon({
	iconUrl : 'icons/ico_spot.png'
});
var targetIcon = new SysIcon({
	iconUrl : 'icons/ico_target.png'
});
var desiredIcon = new SysIcon({
	iconUrl : 'icons/ico_desired.png'
});
var extSysIcon = new SysIcon({
	iconUrl : 'icons/ico_external.png'
});
var planeIcon = new SysIcon({
	iconUrl : 'icons/ico_plane.png'
});
var shipIcon = new SysIcon({
	iconUrl : 'icons/ico_ship.png'
});
var usvIcon = new SysIcon({
	iconUrl : 'icons/ico_usv.png'
});


L.control.locate({
	keepCurrentZoomLevel : true,
	stopFollowingOnDrag : true,
	icon: 'fa fa-map-marker',  // class for icon, fa-location-arrow or fa-map-marker
    iconLoading: 'fa fa-spinner fa-spin',  // class for loading icon
    metric: true,  // use metric or imperial units
    onLocationError: function(err) {alert(err.message)},  // define an error callback function
    onLocationOutsideMapBounds:  function(context) { // called when outside map boundaries
            alert(context.options.strings.outsideMapBoundsMsg);
    }
}).addTo(map);

var nameById = {};

listSystems();

function listSystems() {
	$.ajax({
		cache : false,
		url : "api/v1/systems/",
		dataType : "json",
		success : function(data) {
			$.each(data, function(val) {
				nameById[data[val].imcid] = data[val].name;
			});
		}
	});
}

positionHistory();

function updatePositions() {
	$.ajax({
		cache : false,
		url : "api/v1/systems/active",
		dataType : "json",
		success : function(data) {
			$.each(data, function(val) {
				var coords = data[val].coordinates;
				var name = data[val].name;
				var updated = new Date(data[val].updated_at);
				var ic = sysIcon(data[val].imcid);
				var mins = (new Date() - updated) / 1000 / 60;
				var ellapsed = Math.floor(mins) + " mins ago";
				if (mins > 120) {
					ellapsed = Math.floor(mins / 60) + " hours ago";
				}
				if (mins > 60 * 24 * 2) {
					ellapsed = Math.floor(mins / 60 / 24) + " days ago";
				}
				if (markers[name] == undefined) {
					updates[name] = updated;
					markers[name] = L.marker(coords, {
						icon : ic
					});
					markers[name].bindPopup("<b>" + name + "</b><br/>"
							+ coords[0].toFixed(6) + ", "
							+ coords[1].toFixed(6) + "<hr/>"
							+ updated.toLocaleString() + "<br/>(" + ellapsed
							+ ")");
					markers[name].addTo(map);
				} else {
					if (updates[name] <= updated) {
						markers[name].setLatLng(new L.LatLng(coords[0],
								coords[1]));
						markers[name].bindPopup("<b>" + name + "</b><br/>"
								+ coords[0].toFixed(6) + ", "
								+ coords[1].toFixed(6) + "<hr/>"
								+ updated.toLocaleString() + "<br/>("
								+ ellapsed + ")");
						updates[name] = updated;
						addToTail(name, coords[0], coords[1]);
					}
				}
			});
		}
	});
}

function positionHistory() {
	$.ajax({
		cache : false,
		url : "positions",
		dataType : "json",
		success : function(data) {
			$.each(data, function(val) {

				var lat = data[val].lat;
				var long = data[val].lon;
				var updated = new Date(data[val].timestamp);
				var imc_id = data[val].imc_id;
				var name = nameById[imc_id];
				addToTail(name, lat, long);

			});
		}
	});
}

function sysIconFromName(name) {
	switch (name.toUpperCase()) {
	case "UUV":
		return auvIcon;
	case "UAV":
		return uavIcon;
	case "CCU":
		return ccuIcon;
	case "USV":
		return usvIcon;
	case "STATICSENSOR":
	case "MOBILESENSOR":
		return spotIcon;
	case "MANNED_SHIP":
		return shipIcon;
	case "MANNED_AIRPLANE":
		return planeIcon;
	case "MANNED_CAR":
	case "UGV":
	case "PERSON":
	default:
		return unknownIcon;
	}
}

function sysIcon(imcId) {
	var sys_selector = 0xE000;
	var vtype_selector = 0x1c00;

	if (imcId >= 0x8401 && imcId <= 0x841a)
		return spotIcon;

	// External System
	if (imcId > 0x0000 + 0xFFFF)
		return extSysIcon;
	
	var sys_type = (imcId & sys_selector) >> 13;

	switch (sys_type) {
	case 0:
	case 1:
		switch ((imcId & vtype_selector) >> 10) {
		case 0:
			return auvIcon;
		case 1:
			return unknownIcon; // rov
		case 2:
			return asvIcon; // asv
		case 3:
			return uavIcon;
		default:
			return unknownIcon; // uxv
		}
	case 2:
		return ccuIcon;
	default:
		return unknownIcon;
		break;
	}
}

function addToTail(name, lat, lon) {
	var pos = new L.LatLng(lat, lon);

	if (tails[name] == undefined) {
		tails[name] = L.polyline({});
		tails[name].addTo(map);
	}
	tails[name].addLatLng(pos);
	if (tails[name].getLatLngs().length > 120)
		tails[name].spliceLatLngs(0, 1);
}

updatePositions();
// every minute(60000 millis = 1 min)
setInterval(updatePositions, 60000);

var assets = {};
var lastPositions = {};
var ripplesRef = new Firebase('https://neptus.firebaseio.com/');
ripplesRef.child('assets').on(
		'child_changed',
		function(snapshot) {
			var position = snapshot.val().position;
			var name = snapshot.name();
			var type = snapshot.val().type;
			var lat = position.latitude;
			var lon = position.longitude;

			var plan = snapshot.val().plan;

			if (plan == undefined)
				plans[name] = undefined;
			else {
				if (plan.path != undefined) {
					if (plans[name] == undefined)
						plans[name] = L.polyline({}, {
							color : 'green'
						}).addTo(map);
					else
						plans[name].setLatLngs([]);

					for (point in plan.path) {
						plans[name].addLatLng(L.latLng(plan.path[point]));
					}
				}
			}

			addToTail(name, lat, lon);
			var pos = new L.LatLng(lat, lon);

			if (markers[name] != undefined) {
				markers[name].setLatLng(pos);
				markers[name].setIcon(sysIconFromName(type));
				markers[name].bindPopup("<b>" + name + "</b><br/>"
						+ lat.toFixed(6) + ", " + lon.toFixed(6) + "<hr/>"
						+ new Date().toLocaleString());
				if ("Gunnerus" === name)
					markers[name].setIcon(targetIcon);
			} else {
				markers[name] = L.marker([ lat, lon ], {
					icon : sysIconFromName(type)
				}).bindPopup(
						"<b>" + name + "</b><br/>" + lat.toFixed(6) + ", "
								+ lon.toFixed(6) + "<hr/>"
								+ new Date().toLocaleString());
				markers[name].addTo(map);
			}
		});
