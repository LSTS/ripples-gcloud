$(document).ready(
					function() {
						var d = new Date();
						var month = d.getMonth() + 1;
						var day = d.getDate();
						var date = d.getFullYear() + '-'
								+ (month < 10 ? '0' : '') + month + '-'
								+ (day < 10 ? '0' : '') + day;
						var path = "/logbook/" + date;
						
						$("#export_json").attr("href", path+".json");
						$("#export_md").attr("href", path+".md");
						
						$( "#datepicker" ).datepicker();
						$( "#datepicker" ).datepicker( "option", "dateFormat", "yy-mm-dd" );
						$( "#datepicker" ).datepicker( "setDate", date );
						$( "#datepicker" ).datepicker({ altFormat: "yy-mm-dd"});
						//alert($( "#datepicker" ).datepicker( "option", "altFormat" ));
						$( "#ui-datepicker-div" ).mouseout(function() {
							var selected_date= $("#datepicker").datepicker({ dateFormat: "yy-mm-dd" }).val();
							$.cookie('date', selected_date, { expires: 1 });
							//alert($("#datepicker").datepicker({ dateFormat: "yy-mm-dd" }).val());
							$("#log_title").text("Logbook for "+selected_date+":");
							//alert("cookie:"+$.cookie('date'));
							
							if($.cookie('date')=='')
							{
								path = "/logbook/" + date;
								$("#export_json").attr("href", path+".json");
								$("#export_md").attr("href", path+".md");
							}
							else{
								path = "/logbook/" + $.cookie('date');
								loadContent();
								$("#export_json").attr("href", path+".json");
								$("#export_md").attr("href", path+".md");
							}
						});
						
						//update json load every 5 sec
						setInterval(function(){
							loadContent();
							$("#log_alert").text("");
							}, 5000); // 5000 micro second = 5 sec. 
		
						$.ajax({
									type : 'HEAD',
									url : path,
									success : function() {
										$('#log_view', window.parent.document).hide();
										loadContent();
									},
									error : function() {
										//alert("The logbook for this date "+ date +" needs to be created.");
										$.msgBox({
													title : "Are You Sure",
													content : "Would you like to create a logbook for today?",
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
															//alert("Logbook created.");
															$("#log_alert").text("Logbook created.");
															$('#log_view', window.parent.document).hide();
															loadContent();
														} else {
															$('#log_view', window.parent.document).hide();
														}
													}
												});
									}
								});
						
						//load Content from the selected logbook date
						function loadContent(){
							$.get( path+'.html', function( data ) {
								$( "#pushobj" ).html( data );
								//alert(data);
								}).fail(function() {
									$("#log_alert").text("The requested logbook does not exist.");
								});
						};
						
						//key press events
						$(document).keypress(function(event){
						    var keycode = (event.keyCode ? event.keyCode : event.which);
						    var ok_btn = 'input[type="button"][value="Ok"]';
						    var cancel_btn = 'input[type="button"][value="Cancel"]';
						    
						    if(keycode == '13'){
						    	//alert("Pressed Enter.");
						    	//$(this).closest('form').submit();
						    	if($(ok_btn).is(':visible')) {
						    		$(ok_btn).click();
						    	}
						    }else if(keycode == '27'){
						    	//alert("Pressed Esc.");
						    	if($(cancel_btn).is(':visible')) {
						    		$(cancel_btn).click();
						    	}
						    }

						});
						
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
						        	                //alert(alert_msg);
						        	            	loadContent();
						        	            	$("#log_alert").text(alert_msg);
						        	            },
						        	            error: function( jqXhr, textStatus, errorThrown ){
						        	                console.log( errorThrown );
						        	            }
						        	        });
										} else {
											//alert("empty field.");
											$("#log_alert").text("empty field.");
											}
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
					            		{ header: "Text", type: "text", name: "log_text" }],
					            		buttons: [
					            		{ value: "Ok" }, {value:"Cancel"}],
					            		success: function (result, values) {
						            		if (result == "Ok") {
						            			//check if the form inputs are empty on submit
												if($('input[name=log_text]').val() != ''){
							            		$.ajax({
							        	            url : path+'/log',
							        	            dataType: 'json',
							        	            type: 'POST',
							        	            method: 'POST',
							        	            contentType: 'application/json',
							        	            async:true,
							        	            crossDomain: true,
							        	            data : JSON.stringify({"author": $("#log_user").text(), "text": $('input[name=log_text]').val()}),
							        	            cache: false,
							        	            success: function( data, textStatus, jQxhr ){
							        	                //alert("Log inserted.");
							        	            	loadContent();
							        	            	$("#log_alert").text("Log inserted.");
							        	            },
							        	            error: function( jqXhr, textStatus, errorThrown ){
							        	                console.log( errorThrown );
							        	            }
							        	        });
												} else {
													//alert("empty field.");
													$("#log_alert").text("empty field.");
													}
					            			}
					            			//alert(v);
					            		}
					            		});
					            	
				            	}else if(itemName=="Actions")
				            	{
					            	//alert("Log.");
					            	$.msgBox({ type: "prompt",
					            		title: "Insert Actions",
					            		inputs: [
					            		{ header: "Module", type: "text", name: "log_module" },
					            		{ header: "Text", type: "text", name: "log_text" }],
					            		buttons: [
					            		{ value: "Ok" }, {value:"Cancel"}],
					            		success: function (result, values) {
						            		if (result == "Ok") {
						            			//check if the form inputs are empty on submit
												if($('input[name=log_module]').val() != '' || $('input[name=log_text]').val() != ''){
							            		$.ajax({
							        	            url : path+'/actions',
							        	            dataType: 'json',
							        	            type: 'POST',
							        	            method: 'POST',
							        	            contentType: 'application/json',
							        	            async:true,
							        	            crossDomain: true,
							        	            data : JSON.stringify({"module": $('input[name=log_module]').val().replace(/:$/, ""), "text": $('input[name=log_text]').val()}),
							        	            cache: false,
							        	            success: function( data, textStatus, jQxhr ){
							        	                //alert("Log inserted.");
							        	            	loadContent();
							        	            	$("#log_alert").text("Action inserted.");
							        	            },
							        	            error: function( jqXhr, textStatus, errorThrown ){
							        	                console.log( errorThrown );
							        	            }
							        	        });
												} else {
													//alert("empty field.");
													$("#log_alert").text("empty field.");
													}
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