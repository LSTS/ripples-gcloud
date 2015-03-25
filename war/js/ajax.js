$(document).ready(function() {
	var d = new Date();
	var month = d.getMonth()+1;
	var day = d.getDate();
	var date = d.getFullYear() + '-' + (month<10 ? '0' : '') + month + '-' + (day<10 ? '0' : '') + day;
	
        //$('#add_log').submit(function( event ) {
		$('#logbook_objectives').blur(function(event) {
                /*var place = $('#logbook_place').val();
                //var coords_x = $('#logbook_coord_x').val();
                //var coords_y = $('#logbook_coord_y').val();
                var conditions = $('#logbook_conditions').val();
                var objectives = $('#logbook_objectives').val();
                var team = $('#logbook_team').val();
                var systems = $('#logbook_systems').val();
                //$.get('logbook/'+date+'/', { -> localhost
                $.post('http://ripples.lsts.pt/logbook/'+date+'/objectives', {
                	logbook_place : place,
                	logbook_conditions : conditions,
                	logbook_objectives : objectives,
                	logbook_team : team,
                	logbook_systems : systems
                }, function(responseText) {
                        $('#ajaxGetUserServletResponse').text(responseText);
                });*/
			
			$.ajax({
	            url : 'http://localhost:8888/logbook/'+date+'/log',
	            dataType: 'jsonp',
	            type: 'POST',
	            method: 'POST',
	            contentType: 'application/json',
	            async:true,
	            crossDomain: true,
	            //data : JSON.stringify({objectives : $('#logbook_objectives').val()}),
	            data : JSON.stringify({"author":"teste","text":"texto"}),
	            cache: false,
	            success: function( data, textStatus, jQxhr ){
	                $('#ajaxGetResponse').html( JSON.stringify( data ) );
	            },
	            error: function( jqXhr, textStatus, errorThrown ){
	                console.log( errorThrown );
	            }
	        });
        });
});