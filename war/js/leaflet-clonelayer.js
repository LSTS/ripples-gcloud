/**
 * Minified by jsDelivr using UglifyJS v3.0.24.
 * Original file: /npm/leaflet-clonelayer@1.0.4/index.js
 * 
 * Do NOT use SRI with dynamically generated files! More information: https://www.jsdelivr.com/using-sri-with-dynamic-files
 */
function cloneOptions(e){var n={};for(var r in e){var o=e[r];o&&o.clone?n[r]=o.clone():o instanceof L.Layer?n[r]=cloneLayer(o):n[r]=o}return n}function cloneInnerLayers(e){var n=[];return e.eachLayer(function(e){n.push(cloneLayer(e))}),n}function cloneLayer(e){var n=cloneOptions(e.options);if(e instanceof L.SVG)return L.svg(n);if(e instanceof L.Canvas)return L.canvas(n);if(e instanceof L.TileLayer)return L.tileLayer(e._url,n);if(e instanceof L.ImageOverlay)return L.imageOverlay(e._url,e._bounds,n);if(e instanceof L.Marker)return L.marker(e.getLatLng(),n);if(e instanceof L.Circle)return L.circle(e.getLatLng(),e.getRadius(),n);if(e instanceof L.CircleMarker)return L.circleMarker(e.getLatLng(),n);if(e instanceof L.Rectangle)return L.rectangle(e.getBounds(),n);if(e instanceof L.Polygon)return L.polygon(e.getLatLngs(),n);if(e instanceof L.Polyline)return L.polyline(e.getLatLngs(),n);if(e instanceof L.GeoJSON)return L.geoJson(e.toGeoJSON(),n);if(e instanceof L.LayerGroup)return L.layerGroup(cloneInnerLayers(e));if(e instanceof L.FeatureGroup)return L.FeatureGroup(cloneInnerLayers(e));throw"Unknown layer, cannot clone this layer. Leaflet-version: "+L.version}"object"==typeof exports&&(module.exports=cloneLayer);
//# sourceMappingURL=/sm/2190bf07e19f3572c99f67352cae6f093f168edeb6de7317f892cbfdfd68bfa0.map