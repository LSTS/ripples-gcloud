$(document).ready(function() {
	var d = new Date();
	var month = d.getMonth()+1;
	var day = d.getDate();
	var date = d.getFullYear() + '-' + (month<10 ? '0' : '') + month + '-' + (day<10 ? '0' : '') + day;
	
        $('form#add_log').submit(function( event ) {
		//$( "#other" ).click(function() {
		//$( "#add_log" ).submit();
		//$('#logbook_objectives').blur(function(event) {
			//alert("form submit.");
		//$('#logbook_systems').blur(function(event) {
			$.ajax({
	            url : '/logbook/'+date+'/log',
	            dataType: 'json',
	            type: 'POST',
	            method: 'POST',
	            contentType: 'application/json',
	            async:true,
	            crossDomain: true,
	            //data : JSON.stringify({objectives : $('#logbook_objectives').val()}),
	            data : JSON.stringify({"author":"teste","text":"texto"}),
	            /*data : JSON.stringify({
	            	"place": '"'+$('#logbook_place').val()+'"',
	            	"conditions": '"'+$('#logbook_conditions').val()+'"',
	            	"objectives": '"'+$('#logbook_objectives').val()+'"',
	            	"team": '"'+$('#logbook_team').val()+'"',
	            	"systems": '"'+$('#logbook_systems').val()+'"',
	            	}),*/
	            cache: false,
	            success: function( data, textStatus, jQxhr ){
	                $('#ajaxGetResponse').html( JSON.stringify( data ) );
	                //alert("Added author and text.");
	            },
	            error: function( jqXhr, textStatus, errorThrown ){
	                console.log( errorThrown );
	            }
	        });
        });
});