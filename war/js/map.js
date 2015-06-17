var markers = {};
var updates = {};
var pois = {};
var plans = {};
var tails = {};
var map, marker;
var plotlayers=[];
var selectedMarker;

$.ajaxSetup({ cache: false });
loadPoi();

function loadPoi(){
	var poi_json = $.getJSON( "/poi", function(data) {
	removeMarkers();
	console.log( "success" );
	$.each(data, function(i, item) {
		
		pois[item.description] = item;
		
		//console.log("POI\n:"+JSON.stringify(item));
		
    	var record = {"author": item.author, "description": item.description,"coordinates": [item.coordinates[0], item.coordinates[1]] };
    	marker = new L.marker([item.coordinates[0],item.coordinates[1]],{
    		title: item.description,
    	    contextmenu: true,
    	    contextmenuItems: [{
    	    	text: 'Edit Marker',
    	    	icon: 'images/edit.png',
    	    	callback: editMarker,
    	    	index: 0
    	    	}, {
    	    	separator: true,
    	    	index: 1
    	    	}]});
    	map.addLayer(marker);
    	marker.bindPopup(item.description);
    	marker.on('mouseover', function (e) {
            this.openPopup();
        });
        marker.on('mouseout', function (e) {
            this.closePopup();
        });
        marker.on('contextmenu', function (e) {
        	//console.log('contextmenu: '+e.target.options.title);
        	selectedMarker = e.target.options.title;
        });
    	plotlayers.push(marker);
        record=null;
    });
	/* test json load methods
	})
	.done(function() {
	console.log( "done" );
	})
	.fail(function() {
	console.log( "error" );
	})
	.always(function() {
	console.log( "complete" );*/
	});
};

function removeMarkers() {
	for (i=0;i<plotlayers.length;i++) {
		map.removeLayer(plotlayers[i]);
	}
	plotlayers=[];
	pois =  {};
}

setInterval(function(){
	//alert("update");
	removeMarkers();
	loadPoi();
	//$("#log_alert").text("");
	}, 60000); // 60000 milliseconds = one minute

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

function editMarker (e) {
	//alert("show edit popup for "+selectedMarker);
	console.log("props for marker: "+JSON.stringify(pois[selectedMarker]));
	var lat=pois[selectedMarker].coordinates[0], lng=pois[selectedMarker].coordinates[1];
	$.msgBox({ type: "prompt",
		title: "Update Point of Interest",
		inputs: [
		{ header: "Description:", type: "text", name: "description_text", value: pois[selectedMarker].description }],
		buttons: [
		{ value: "Ok" }, {value:"Cancel"}],
		success: function (result, values) {
    		if (result == "Ok") {
    			//check if the form inputs are empty on submit
				if($('input[name=description_text]').val() != ''){
				var val = {"author": $("#log_user").text(), "description": $('input[name=description_text]').val(),"coordinates": [lat, lng] };
	    	$.ajax({
	            url : '/poi',
	            dataType: 'json',
	            type: 'POST',
	            method: 'POST',
	            contentType: 'application/json',
	            async:true,
	            crossDomain: true,
	            data : JSON.stringify(val),
	            cache: false,
	            success: function( data, textStatus, jQxhr ){
	            	//console.log(data);
	            	pois[data.description] = data;
	            	
	            	console.log("POI\n:"+JSON.stringify(data));
	            	 //alert("Point of interest inserted.");
	            	marker = new L.marker([lat,lng],{
	            		title:data.description,
	            	    contextmenu: true,
	            	    contextmenuItems: [{
	            	    	text: 'Edit Marker',
	            	    	icon: 'images/edit.png',
	            	    	callback: editMarker,
	            	    	index: 0
	            	    	}, {
	            	    	separator: true,
	            	    	index: 1
	            	    	}]}).bindPopup($('input[name=description_text]').val());
	            	map.addLayer(marker);
	            	marker.on('mouseover', function (e) {
	                    this.openPopup();
	                });
	                marker.on('mouseout', function (e) {
	                    this.closePopup();
	                });
	                marker.on('contextmenu', function (e) {
	                	//console.log('contextmenu: '+e.target.options.title);
	                	selectedMarker = e.target.options.title;
	                });
	            	plotlayers.push(marker);
	            	val=null;
	            },
	            error: function( jqXhr, textStatus, errorThrown ){
	                console.log( errorThrown );
	            }
	        });
				} else {
					alert("empty field.");
					}
			}
			//alert(v);
		}
		});
}

function addMarker (e) {
	var lat = e.latlng.lat;
	var lng = e.latlng.lng;
	$.msgBox({ type: "prompt",
		title: "Insert Point of Interest",
		inputs: [
		{ header: "Description:", type: "text", name: "description_text" }],
		buttons: [
		{ value: "Ok" }, {value:"Cancel"}],
		success: function (result, values) {
    		if (result == "Ok") {
    			//check if the form inputs are empty on submit
				if($('input[name=description_text]').val() != ''){
				var val = {"author": $("#log_user").text(), "description": $('input[name=description_text]').val(),"coordinates": [lat, lng] };
	    	$.ajax({
	            url : '/poi',
	            dataType: 'json',
	            type: 'POST',
	            method: 'POST',
	            contentType: 'application/json',
	            async:true,
	            crossDomain: true,
	            data : JSON.stringify(val),
	            cache: false,
	            success: function( data, textStatus, jQxhr ){
	            	//console.log(data);
	            	pois[data.description] = data;
	            	
	            	console.log("POI\n:"+JSON.stringify(data));
	            	
	                //alert("Point of interest inserted.");
	            	marker = new L.marker([lat,lng],{
	            		title:data.description,
	            	    contextmenu: true,
	            	    contextmenuItems: [{
	            	    	text: 'Edit Marker',
	            	    	icon: 'images/edit.png',
	            	    	callback: editMarker,
	            	    	index: 0
	            	    	}, {
	            	    	separator: true,
	            	    	index: 1
	            	    	}]}).bindPopup($('input[name=description_text]').val());
	            	map.addLayer(marker);
	            	marker.on('mouseover', function (e) {
	                    this.openPopup();
	                });
	                marker.on('mouseout', function (e) {
	                    this.closePopup();
	                });
	                marker.on('contextmenu', function (e) {
	                	//console.log('contextmenu: '+e.target.options.title);
	                	selectedMarker = e.target.options.title;
	                });
	            	plotlayers.push(marker);
	            	val=null;
	            },
	            error: function( jqXhr, textStatus, errorThrown ){
	                console.log( errorThrown );
	            }
	        });
				} else {
					alert("empty field.");
					}
			}
			//alert(v);
		}
		});
}

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

var map = L.map('map', {
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

var kmlLayer = new L.KML("/kml/file.kmz", {async: true});
                                       
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

function activeSystem (imc_id){
	$.ajax({
	    cache: false,
	    url: "api/v1/systems/active",
	    dataType: "json",
	    success: function(data) {
	      $.each(data, function(val) {
	    	var imcid = data[val].imcid;
			var name = data[val].name;
			if(imc_id==imcid){
				return name;
			}
	  	});
		}
	});
}

function updatePositions() {
	$.ajax({
	    cache: false,
	    //url: "api/v1/systems/active",
	    url: "positions",
	    dataType: "json",
	    success: function(data) {
	      $.each(data, function(val) {
			var lat = data[val].lat;
			var long = data[val].lon;
			//var name = "active system";
			var updated = new Date(data[val].timestamp);
			var imc_id = data[val].imc_id;
			var ic = sysIcon(imc_id);
			var name = activeSystem(imc_id);
			var mins = (new Date() - updated) / 1000 / 60;
			var ellapsed = Math.floor(mins) + " mins ago";
			var polylinePoints = [];
			var cols = [];
			var rows = data.length;
			for (var i  = 0; i < rows; i++){
				polylinePoints.push([data[i].lat,data[i].lon]);
				
			}
			L.polyline(polylinePoints,
			{
				color: '#174E64',
				weight: 3,
				opacity: 0.5,
				smoothFactor: 1,
				dashArray:"1,9"
			}).addTo(map);
			
			if (mins > 120) {
				ellapsed = Math.floor(mins / 60) + " hours ago";
			}

			if (mins > 60 * 24 * 2) {
				ellapsed = Math.floor(mins / 60 / 24) + " days ago";
			}

			if (markers[name] == undefined) {
				updates[name] = updated;
				markers[name] = L.marker([lat,long], {
					icon : ic
				});
				markers[name].bindPopup("<b>" + name + "</b><br/>"
						+ lat.toFixed(6) + ", " + long.toFixed(6)
						+ "<hr/>" + updated.toLocaleString() + "<br/>("
						+ ellapsed + ")");
				markers[name].addTo(map);
			} else {
				if (updates[name] <= updated) {
					markers[name].setLatLng(new L.LatLng(lat, long));
					markers[name].bindPopup("<b>" + name + "</b><br/>"
							+ lat.toFixed(6) + ", " + long.toFixed(6)
							+ "<hr/>" + updated.toLocaleString() + "<br/>("
							+ ellapsed + ")");
					updates[name] = updated;
				}
			}
		});
	}});
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
updatePositions();
setInterval(updatePositions, 60000);

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
