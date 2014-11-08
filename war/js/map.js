

var markers = {};

var hybrid = L.tileLayer(
		'https://{s}.tiles.mapbox.com/v3/{id}/{z}/{x}/{y}.png',
				{
					maxZoom : 22,
					attribution : 'Map data &copy; <a href="http://openstreetmap.org">OpenStreetMap</a>',
					id : 'examples.map-i875mjb7'
				});

var streets = L.tileLayer(
		'https://{s}.tiles.mapbox.com/v3/{id}/{z}/{x}/{y}.png',
		{
			maxZoom : 22,
			attribution : 'Map data &copy; <a href="http://openstreetmap.org">OpenStreetMap</a>',
			id : 'examples.map-20v6611k'
		});

var Esri_OceanBasemap = L.tileLayer('http://server.arcgisonline.com/ArcGIS/rest/services/Ocean_Basemap/MapServer/tile/{z}/{y}/{x}', {
	attribution: 'Tiles &copy; ESRI',
	maxZoom: 13
});

var Esri_WorldImagery = L.tileLayer('http://server.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile/{z}/{y}/{x}', {
	attribution: 'Tiles &copy; ESRI'
});

var osmLayer = new L.TileLayer('http://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {maxZoom: 23, attribution: 'Map data &copy; OpenStreetMap contributors, CC-BY-SA'});

var map = L.map('map', {center:[ 41.185356, -8.704898 ], zoom: 13, layers: [osmLayer]});

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

var cloudsLayer = L.tileLayer('http://{s}.tile.openweathermap.org/map/clouds/{z}/{x}/{y}.png', {
    attribution: 'Map data &copy; OpenWeatherMap',
    maxZoom: 23,
    opacity: 0.7,
    maxNativeZoom: 18
});


var windLayer = L.tileLayer('http://{s}.tile.openweathermap.org/map/wind/{z}/{x}/{y}.png', {
    attribution: 'Map data &copy; OpenWeatherMap',
    maxZoom: 23,
    opacity: 0.7,
    maxNativeZoom: 18
});

var precipLayer = L.tileLayer('http://{s}.tile.openweathermap.org/map/precipitation/{z}/{x}/{y}.png', {
    attribution: 'Map data &copy; OpenWeatherMap',
    maxZoom: 23,
    opacity: 0.7,
    maxNativeZoom: 18
});


var baseLayers = {
		"Open Street Map": osmLayer,
		"Grayscale": streets,
		"Terrain": hybrid,
		"ESRI Ocean": Esri_OceanBasemap,
		"ESRI Aerial": Esri_WorldImagery
	};

var overlays = {
	"Cloud cover" : cloudsLayer,
	"Wind speed" : windLayer,
	"Precipitation": precipLayer
}

L.control.layers(baseLayers, overlays).addTo(map);

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

function updatePositions() {
	$.getJSON("api/v1/systems", function(data) {

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
				markers[name] = L.marker(coords, {
					icon : ic
				});
				markers[name].bindPopup("<b>" + name + "</b><br/>"
						+ coords[0].toFixed(6) + ", " + coords[1].toFixed(6)
						+ "<hr/>" + updated.toLocaleString() + "<br/>("
						+ ellapsed + ")");
				markers[name].addTo(map);
			} else {
				console.info(markers[name].getLatLng());
				markers[name].setLatLng(new L.LatLng(coords[0], coords[1]));
				markers[name].bindPopup("<b>" + name + "</b><br/>"
						+ coords[0].toFixed(6) + ", " + coords[1].toFixed(6)
						+ "<hr/>" + updated.toLocaleString() + "<br/>("
						+ ellapsed + ")");
			}
		});
	});
}

function sysIconFromName(name) {
	switch (name) {
	case "UUV":
		return auvIcon;
	case "UAV":
		return uavIcon;
	case "CCU":
		return ccuIcon;
	default:
		return unknownIcon;
	}
}

function sysIcon(imcId) {
	var sys_selector = 0xE000;
	var vtype_selector = 0x1800;

	if (imcId >= 0x8401 && imcId <= 0x841a)
		return spotIcon;

	var sys_type = (imcId & sys_selector) >> 13;

	switch (sys_type) {
	case 0:
	case 1:
		switch ((imcId & vtype_selector) >> 11) {
		case 0:
			return auvIcon;
		case 1:
			return unknownIcon; // rov
		case 2:
			return unknownIcon; // asv
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

setInterval(updatePositions(), 10000);
var assets = {};
var lastPositions = {};
var ripplesRef = new Firebase('https://neptus.firebaseIO-demo.com/');
ripplesRef.child('assets').on(
		'child_changed',
		function(snapshot) {
			var position = snapshot.val().position;
			var name = snapshot.name();
			var type = snapshot.val().type;
			var lat = position.latitude;
			var lon = position.longitude;

			if (markers[name] != undefined) {
				markers[name].setLatLng(new L.LatLng(lat, lon));
				markers[name].bindPopup("<b>" + name + "</b><br/>"
						+ lat.toFixed(6) + ", " + lon.toFixed(6) + "<hr/>"
						+ new Date().toLocaleString());
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