var log_author = {};
var log_text = {};

$(window).load(function () {
	
function resize(){$('#notifications').height(window.innerHeight - 50);}
$( window ).resize(function() {resize();});
resize();

function refresh_close(){
$('.close').click(function(){$(this).parent().fadeOut(200);});
}
refresh_close();

function loadData(){
	var path = "/logbook/rt";
	
	var log_entry_json = $.getJSON(path, function(data) {
		$.each(data, function(i, item) {

			log_author[i]=item.author;
			log_text[i] = item.text;
			
			if(log_author[i]!="" || log_text[i]!=""){
				var note = new Notification();
				note.init("log_popup_"+i,log_author[i],log_text[i]);
			}
			
		});
	});
}
loadData();

/*Check every minute new log entries to create notifications popups*/
setInterval(loadData,60000);

var Notification = function(){};
Notification.prototype = {
	  init: function(widget_name, author, text){

	    this.widget_name = widget_name;
	    
	    var bottom_center = '<div id='+widget_name+' class="notifications-bottom-right-tab">'+
	    '<div id="notifications-bottom-right-tab-close" class="close"><span>x</span></div>'+
	    '<a href="/log.jsp"><div id="notifications-bottom-right-tab-avatar"><img src="images/logo_hub.png" '+
	    'width="84" height="60" /></div><div id="notifications-bottom-right-tab-right">'+
	    '<div id="notifications-bottom-right-tab-right-title">entry from <span>'+author+'</span>'+
	    '</div><div id="notifications-bottom-right-tab-right-text">'+text+'</div></a></div></div>';
	    
	    $("#notifications-bottom-right").html();
	    $("#notifications-bottom-right").append(bottom_center);
	    $("#"+widget_name).addClass('animated bounceInRight');
	    refresh_close();
	    setTimeout(function(self){self.fadeOutUp(widget_name)}, 10000, this);
	  },

	  fadeOutUp: function(widget_name) {
		  $("#"+widget_name).removeClass('animated bounceInRight').addClass('animated fadeOutUp');
	  }
	};

});