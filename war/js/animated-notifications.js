var log_entry = {};

$(window).load(function () {
	
function resize(){$('#notifications').height(window.innerHeight - 50);}
$( window ).resize(function() {resize();});
resize();

function refresh_close(){
$('.close').click(function(){$(this).parent().fadeOut(200);});
}
refresh_close();

function loadData(){
	var d = new Date();
	var month = d.getMonth() + 1;
	var day = d.getDate();
	var date = d.getFullYear() + '-'
			+ (month < 10 ? '0' : '') + month + '-'
			+ (day < 10 ? '0' : '') + day;
	var path = "/logbook/" +date+".json";
	
	var log_entry_json = $.getJSON(path, function(data) {
		$.each(data, function(i, item) {
			log_entry[item.log] = item;
			console.log("log_entry["+i+"]: "+log_entry[item.log]);
		});
	});	
}
loadData();

var Notification = function(){};
Notification.prototype = {
	  init: function(widget_name){

	    this.widget_name = widget_name;
	    console.log("name: "+widget_name);
	    
	    var bottom_center = '<div id='+widget_name+' class="notifications-bottom-right-tab"><div id="notifications-bottom-right-tab-close" class="close"><span>x</span></div><div id="notifications-bottom-right-tab-avatar"><img src="images/logo_hub.png" width="84" height="60" /></div><div id="notifications-bottom-right-tab-right"><div id="notifications-bottom-right-tab-right-title"><span>Ripples '+widget_name+'</span> sent you a message</div><div id="notifications-bottom-right-tab-right-text">This is a sample notification that <br> will appear the right bottom corner.</div></div></div>';
	    
	    $("#notifications-bottom-right").html();
	    $("#notifications-bottom-right").append(bottom_center);
	    //$("#notifications-bottom-right").html('<div id="x" style="width:100x;height:10px;background-color:red"></div>');
	    $("#"+widget_name).addClass('animated bounceInRight');
	    refresh_close();
	    setTimeout(function(self){self.fadeOutUp(widget_name)}, 3000, this);
	  },

	  fadeOutUp: function(widget_name) {
		  //console.log("#"+widget_name);
		  $("#"+widget_name).removeClass('animated bounceInRight').addClass('animated fadeOutUp');
		  console.log("fadeOutUp");
	  }
	};

var note = new Notification();
note.init("z");

});