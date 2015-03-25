$(document).ready(function() {
	//alert("x");
    $('#submit').click(function () {
	//$('#test').blur(function(event) {
        $.ajax({
            url: '/logbook/create',
            type: 'POST',
            method: 'POST',
            data: "create_log",
            async:true,
            crossDomain: true,
            success: function( data, textStatus, jQxhr ){
                $('#ajaxGetResponse').html("created log.");
                alert("The log is created.");
            },
            error: function( jqXhr, textStatus, errorThrown ){
                console.log( errorThrown );
                $('#ajaxGetResponse').html("the log is already here.");
                alert("The log for this day is already created.");
            }
        });
    });
    return false;
});