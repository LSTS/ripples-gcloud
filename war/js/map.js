var markers = {};
var plans = {};
var tails = {};
var map, marker;
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

function showCoordinates (e) {
    alert(e.latlng);
}

function centerMap (e) {
    map.panTo(e.latlng);
}

function zoomIn (e) {
    map.zoomIn();
}

function zoomOut (e) {
    map.zoomOut();
}

function openPopup (e) {
	marker.openPopup();
}

//map = L.map('map', {center:[ 41.185356, -8.704898 ], zoom: 13, layers: [osmLayer]});
map = L.map('map', {
    center: [ 41.185356, -8.704898 ],
    zoom: 13,
    layers: [osmLayer],
    contextmenu: true,
    contextmenuWidth: 140,
    contextmenuItems: [{
        text: 'Add Marker',
        icon: 'images/add_pin.png',
        callback: addMarker,
        index: 0
    }, {
        separator: true,
        index: 1
    }, {
	      text: 'Show coordinates',
	      callback: showCoordinates
    }, {
	      text: 'Center map here',
	      callback: centerMap
    }, '-', {
	      text: 'Zoom in',
	      icon: 'images/zoom-in.png',
	      callback: zoomIn
    }, {
	      text: 'Zoom out',
	      icon: 'images/zoom-out.png',
	      callback: zoomOut
    }]     
});

var ll = new L.LatLng(41.18146, -8.70521);
marker = L.marker(ll, {
    contextmenu: true,
    contextmenuItems: [{
        text: 'Edit Marker',
        icon: 'images/edit.png',
        callback: openPopup,
        index: 0
    }, {
        separator: true,
        index: 1
    }]
}).addTo(map).bindPopup('<strong>Popup KML marker</strong><br>Here is the text, this marker has author, description and coordinates.');

/*var pin_location = [
      		["marker01",40.99497,-7.50808],
      		["marker02",41.30269,-7.63696],
      		["marker03",41.49413,-7.5421],
      		["marker04",40.98585,-7.50659],
      		["marker05",40.93163,-7.81726],
      		["marker06",41.5183,-7.78081],
      		["marker07",41.42079,-7.5783],
      		["marker08",42.08414,-7.96632],
      		["marker09",41.51285,-7.53274]
      		];*/

/*for (var i = 0; i < pin_location.length; i++) {
markerArray = new L.marker([pin_location[i][1],pin_location[i][2]])
	.bindPopup(pin_location[i][0])
	.addTo(map);
}*/



function addMarker () {
	//alert("open");
	var pin_location = [[,,]];
	//alert($("div").hasClass("a.leaflet-contextmenu-item"));
	
	//alert($("div").find('a').hasClass("leaflet-contextmenu-item"));
	
	map.on('click', function(e) {
		//alert("map");
	    var lat = e.latlng.lat;
	    var lng = e.latlng.lng;
	    var count = 0;
	    //alert ("Latitude : " + lat + "\nLongitude : " + lng);
	    for (var i = 0; i < pin_location.length; i++) {
	    	count+=1;
	    	
	    	pin_location[i][0]="marker"+count;
	    	pin_location[i][1]=lat;
	    	pin_location[i][2]=lng;
	    	
	    	$.ajax({
	            url : '/poi',
	            dataType: 'json',
	            type: 'POST',
	            method: 'POST',
	            contentType: 'application/json',
	            async:true,
	            crossDomain: true,
	            data : JSON.stringify({"author": $("#log_user").text(), "description": pin_location[i][0],"coordinates": [pin_location[i][1],pin_location[i][2]] }),
	            cache: false,
	            success: function( data, textStatus, jQxhr ){
	                alert("Point of interest inserted.");
	            	//loadContent();
	            	//$("#log_alert").text("Log inserted.");
	            },
	            error: function( jqXhr, textStatus, errorThrown ){
	                console.log( errorThrown );
	            }
	        });
	    	
	    	markerArray = new L.marker([pin_location[i][1],pin_location[i][2]])
	    		.bindPopup(pin_location[i][0])
	    		.addTo(map);
	    }
	});
}


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

L.control.locate({keepCurrentZoomLevel: true, stopFollowingOnDrag: true}).addTo(map);

function updatePositions() {
	$.getJSON("api/v1/systems/active", function(data) {

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
	var vtype_selector = 0x1c00;

	if (imcId >= 0x8401 && imcId <= 0x841a)
		return spotIcon;

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

			var plan = snapshot.val().plan;
			
			if (plan == undefined)
				plans[name] = undefined;
			else { 
				if (plan.path != undefined) {
					if (plans[name] == undefined)
						plans[name] = L.polyline({}, {color: 'green'}).addTo(map);
					else 
						plans[name].setLatLngs([]);
					
					for (point in plan.path) {
						plans[name].addLatLng(L.latLng(plan.path[point]));
					}
				}				
			}
			
			var pos = new L.LatLng(lat, lon);
			
			if (tails[name] == undefined) {
				tails[name] = L.polyline({});
				tails[name].addTo(map);				
			}
			tails[name].addLatLng(pos);
			if (tails[name].getLatLngs().length > 120)
				tails[name].spliceLatLngs(0, 1);
			
			if (markers[name] != undefined) {
				markers[name].setLatLng(pos);
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

/*map.on('contextmenu',function(e){
    //alert("plugin run!");
	
	$(function(){
	    $.contextMenu({
	        selector: '.context-menu-one', 
	        callback: function(key, options) {
	            var m = "global: " + key;
	            window.console && console.log(m) || alert(m); 
	        },
	        items: {
	            "edit": {
	                name: "Edit", 
	                icon: "edit", 
	                // superseeds "global" callback
	                callback: function(key, options) {
	                    var m = "edit was clicked";
	                    window.console && console.log(m) || alert(m); 
	                }
	            },
	            "cut": {name: "Cut", icon: "cut"},
	            "copy": {name: "Copy", icon: "copy"},
	            "paste": {name: "Paste", icon: "paste"},
	            "delete": {name: "Delete", icon: "delete"},
	            "sep1": "---------",
	            "quit": {name: "Quit", icon: "quit"}
	        }
	    });
	});
	
});*/