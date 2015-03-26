$(document).ready(function() {
	var d = new Date();
	var month = d.getMonth()+1;
	var day = d.getDate();
	var date = d.getFullYear() + '-' + (month<10 ? '0' : '') + month + '-' + (day<10 ? '0' : '') + day;
	
        $('form#add_place').submit(function( event ) {
        	//alert("hello");
        	//alert($('#logbook_place').val());
			$.ajax({
	            url : '/logbook/'+date+'/place',
	            dataType: 'json',
	            type: 'POST',
	            method: 'POST',
	            contentType: 'application/json',
	            async:true,
	            crossDomain: true,
	            data : JSON.stringify($('#logbook_place').val()),
	            cache: false,
	            success: function( data, textStatus, jQxhr ){
	                //$('#ajaxGetResponse').html( JSON.stringify( data ) );
	                alert("Added place.");
	            },
	            error: function( jqXhr, textStatus, errorThrown ){
	                console.log( errorThrown );
	            }
	        });
        });
});