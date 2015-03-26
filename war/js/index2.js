$(document).ready(function(){
	// HTML markup implementation, overlap mode, initilaize collapsed
	$( '#menu' ).multilevelpushmenu({
		containersToPush: [$( '#pushobj' )],
		collapsed: true,

		// Just for fun also changing the look of the menu
		wrapperClass: 'mlpm_w',
		menuInactiveClass: 'mlpm_inactive',
		
		onItemClick: function() {
            var event = arguments[0],
                $menuLevelHolder = arguments[1],
                $item = arguments[2],
                options = arguments[3],
                title = $menuLevelHolder.find( 'h2:first' ).text(),
                itemName = $item.find( 'a:first' ).text();

                var location = window.location.protocol + "//" + window.location.host + "/";
            
            	if(itemName=="Map")
            	{
            		$("#contentIframe").show();
            		$("#contentIframe").attr('src',location);   
            	}
            	else if(itemName=="Servlets")
            	{
            		$("#contentIframe").show();
            		$("#contentIframe").attr('src',location+"servlets.html");   
            	}
            	else if(itemName=="Create")
            	{
            		$("#contentIframe").show();
            		$("#contentIframe").attr('src',location+"create_log.jsp");   
            	}
            	else if(itemName=="Place")
            	{
            		$("#contentIframe").show();
            		$("#contentIframe").attr('src',location+"add_place.html");   
            	}
            	else if(itemName=="Log")
            	{
            		$("#contentIframe").show();
            		$("#contentIframe").attr('src',location+"add_log.jsp");   
            	}
            	else if(itemName=="History")
            	{
            		$("#contentIframe").show();
            		$("#contentIframe").attr('src',location+"logbook/");   
            	}
            	else{
            		$("#contentIframe").hide();
            		alert("Empty.");
            	}
            //$( '#eventpanel' ).append( '<br />Item <i>' + itemName + '</i>' + ' on <i>' + title + '</i> menu level clicked!' );
            console.log(arguments);
        }
		
	});
	$( window ).resize(function() {
	    $( '#menu' ).multilevelpushmenu( 'redraw' );
	});
});