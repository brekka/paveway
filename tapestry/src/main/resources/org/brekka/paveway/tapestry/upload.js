var PegasusUpload = {
    ready : {},
	apply : function (id, uploadLink, pMaxFiles, pMaxFileSize, pMaxChunkSize) {
		if (!(window.File && window.FileReader && window.FileList && window.Blob)) {
			return;
		}
		var inputId = 'fileupload_' + id;
		var context = $('#' + id);
		context.find(".failsafe-input").remove();
		var multiple = (navigator.userAgent.indexOf("Firefox") == -1);
		context.find('span.add_files').html('<input id="' + inputId + '" type="file" name="files[]" multiple="' + multiple + '" />');
		context.find('div.multi-upload').removeClass("hidden");
		var table = context.find('table');
		if (table.find('tbody tr').size() == 0) {
			table.hide();
		}
		table.removeClass("hidden");
		var cnt = 0, total = 0;
		$('#' + inputId).fileupload({
	    	maxChunkSize: pMaxChunkSize,
	        dataType: 'json',
	        url: uploadLink,
	        done: function (e, data) {
	        	$.each(data.files, function (index, file) {
	        		$(file.progress).text("100%");
	        		cnt --;
	            });
	        	if (cnt == 0) {
	        		PegasusUpload.ready[inputId] = true;
	        	}
	        },
	        add: function (e, data) {
	        	if (total >= pMaxFiles) {
	        		return false;
	        	}
	        	table.show();
	        	//$('#files tbody').empty();
	        	for (var i = 0; i < data.files.length; i++) {
	        		var file = data.files[i];
	        		if (file.size > pMaxFileSize) {
	        			return false;
	        		}
	        		table.find('tbody').append('<tr><td>' + file.name + '</td><td>' + file.size + '</td><td class="progress">0%</td></tr>');
	        		file.progress = table.find('tr:last .progress');
	        		cnt ++;
	        		total++;
	        	}
	        	data.submit();
	        },
	        progress : function (e, data) {
	        	var p = parseInt(data.loaded / data.total * 100, 10);
	        	$.each(data.files, function (index, file) {
	        		$(file.progress).text(p + "%");
	            });
	        },
	        start: function (e) {
	        	PegasusUpload.ready[inputId] = false;
	        },
	    });
	}	
};
