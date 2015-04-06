$(document).ready(
					function() {
						var d = new Date();
						var month = d.getMonth() + 1;
						var day = d.getDate();
						var date = d.getFullYear() + '-'
								+ (month < 10 ? '0' : '') + month + '-'
								+ (day < 10 ? '0' : '') + day;
						var path = "/logbook/" + date;
						
						//update json load every 5 sec
						setInterval(function(){
							$.ajax({
							url: path+'.json',
							}).done(function() {
							//alert( "done" );
								$('.log_list').remove();
								updateListJSON();
							});
							}, 5000); // 5000 micro second = 5 sec. 
		
						$.ajax({
									type : 'HEAD',
									url : path,
									success : function() {
										//alert("The logbook for this date " + date + " exists.");
										//$("#log_view").attr('src', path);
										$('#log_view', window.parent.document).hide();
										updateListJSON();
									},
									error : function() {
										//alert("The logbook for this date "+ date +" needs to be created.");
										$.msgBox({
													title : "Are You Sure",
													content : "Would you like to create a logobook for today?",
													type : "confirm",
													buttons : [ {
														value : "Yes"
													}, {
														value : "No"
													}, {
														value : "Cancel"
													} ],
													success : function(result) {
														if (result == "Yes") {
															var newpath = '/logbook/create';
															$('#log_view', window.parent.document).show();
															$("#log_view").attr('src', newpath);
															alert("Logbook created.");
															$('#log_view', window.parent.document).hide();
															window.location.reload();
														} else {
															//$("#log_view").attr('src', '/logbook/');
															$('#log_view', window.parent.document).hide();
														}
													}
												});
									}
								});
							
						//update of the JSON List 
						function updateListJSON() {
							$.getJSON( path+'.json', function( data ) {
								var items = [];
								$.each( data, function( key, val ) {
									if(key=="log"){
										//console.log(this);
										for (var log in this) {
											items.push( "<li id='" + key + "'>"+key+": "+this[log].author+" - "+ this[log].text + "</li>" );
										}
									}else if(key=="coords" || key=="actions"){}
									else{
										items.push( "<li id='" + key + "'>"+key+": "+ val + "</li>" );
									}
								});
								$( "<ul/>", {
								"class": "log_list",
								html: items.join( "" )
								}).appendTo( "#pushobj" );
								});
						}	
						
						//messageBox
						function messageBox(msg_title, input_header, input_name, json_url, alert_msg) {
							$.msgBox({ type: "prompt",
			            		title: msg_title,
			            		inputs: [
			            		{ header: input_header, type: "text", name: input_name }],
			            		buttons: [
			            		{ value: "Ok" }, {value:"Cancel"}],
			            		success: function (result, values) {
				            		if (result == "Ok") {
				            			//check if the form inputs are empty on submit
										if($('input[name='+input_name+']').val() != ''){
						            		$.ajax({
						        	            url : path+json_url,
						        	            dataType: 'json',
						        	            type: 'POST',
						        	            method: 'POST',
						        	            contentType: 'application/json',
						        	            async:true,
						        	            crossDomain: true,
						        	            data : JSON.stringify($('input[name='+input_name+']').val()),
						        	            cache: false,
						        	            success: function( data, textStatus, jQxhr ){
						        	                alert(alert_msg);
						        	                window.location.reload();
						        	            },
						        	            error: function( jqXhr, textStatus, errorThrown ){
						        	                console.log( errorThrown );
						        	            }
						        	        });
										} else {alert("empty field.");}
			            			}
			            			//alert(v);
			            		}
			            		});
						}
						
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
					            /*if(itemName=="Map")
				            	{
					            	alert("Map.");
				            	}
					            else*/ if(itemName=="Place")
				            	{
					            	//alert("Place.");
					            	messageBox("Insert Place", "Place", "log_place", "/place", "Added Place.");
				            	}
					            else if(itemName=="Conditions")
				            	{
					            	//alert("Conditions."); 
					            	messageBox("Insert Conditions", "Conditions", "log_conditions", "/conditions", "Added Condition.");
				            	}
					            else if(itemName=="Objectives")
				            	{
					            	//alert("Objectives."); 
					            	messageBox("Insert Objectives", "Objectives", "log_objectives", "/objectives", "Added Objective.");
				            	}
					            else if(itemName=="Team")
				            	{
					            	//alert("Team."); 
					            	messageBox("Insert Team", "Team", "log_team", "/team", "Added Team member.");
				            	}
					            else if(itemName=="Systems")
				            	{
					            	//alert("Systems."); 
					            	messageBox("Insert Systems", "Systems", "log_systems", "/systems", "Added System.");
				            	}
					            else if(itemName=="Log")
				            	{
					            	//alert("Log.");
					            	$.msgBox({ type: "prompt",
					            		title: "Insert Log",
					            		inputs: [
					            		{ header: "Author", type: "text", name: "log_author" },
					            		{ header: "Text", type: "text", name: "log_text" }],
					            		buttons: [
					            		{ value: "Ok" }, {value:"Cancel"}],
					            		success: function (result, values) {
						            		if (result == "Ok") {
						            			//check if the form inputs are empty on submit
												if($('input[name=log_author]').val() != '' || $('input[name=log_text]').val() != ''){
							            		$.ajax({
							        	            url : path+'/log',
							        	            dataType: 'json',
							        	            type: 'POST',
							        	            method: 'POST',
							        	            contentType: 'application/json',
							        	            async:true,
							        	            crossDomain: true,
							        	            data : JSON.stringify({"author": $('input[name=log_author]').val(), "text": $('input[name=log_text]').val()}),
							        	            cache: false,
							        	            success: function( data, textStatus, jQxhr ){
							        	                alert("Log inserted.");
							        	            },
							        	            error: function( jqXhr, textStatus, errorThrown ){
							        	                console.log( errorThrown );
							        	            }
							        	        });
												} else {alert("empty field.");}
					            			}
					            			//alert(v);
					            		}
					            		});
					            	
				            	}
					            /*else{
				            		alert("Empty.");
				            	}*/
					            //$( '#eventpanel' ).append( '<br />Item <i>' + itemName + '</i>' + ' on <i>' + title + '</i> menu level clicked!' );
					            console.log(arguments);
					        }
							
						});
						$( window ).resize(function() {
						    $( '#menu' ).multilevelpushmenu( 'redraw' );
						});
						
						
					});