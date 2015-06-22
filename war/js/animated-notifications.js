$(window).load(function () {
	
function resize(){$('#notifications').height(window.innerHeight - 50);}
$( window ).resize(function() {resize();});
resize();

function refresh_close(){
$('.close').click(function(){$(this).parent().fadeOut(200);});
}
refresh_close();
 
var bottom_center = '<div id="notifications-bottom-right-tab"><div id="notifications-bottom-right-tab-close" class="close"><span>x</span></div><div id="notifications-bottom-right-tab-avatar"><img src="images/logo_hub.png" width="84" height="60" /></div><div id="notifications-bottom-right-tab-right"><div id="notifications-bottom-right-tab-right-title"><span>Ripples</span> sent you a message</div><div id="notifications-bottom-right-tab-right-text">This is a sample notification that <br> will appear the right bottom corner.</div></div></div>';

var Notification = function(){};
Notification.prototype = {
	  init: function(widget_name){
	    this.widget_name = widget_name;
	    console.log("name: "+widget_name);
	    $("#notifications-bottom-right").html();
	    $("#notifications-bottom-right").html(bottom_center);
	    $("#notifications-bottom-right-tab").addClass('animated bounceInRight');
	    refresh_close();
	    setTimeout(this.fadeOutUp, 2000);
	  },

	  fadeOutUp: function() {
		  $("#notifications-bottom-right-tab").removeClass('animated ' + $('#effects').val()).addClass('animated fadeOutUp');
		  console.log("fadeOutUp");
	  }
	};

var note = new Notification();
note.init("z");

});