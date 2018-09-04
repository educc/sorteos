var Utils = {};
(function(THIS){

        THIS.MONTHS = ["Ene", "Feb", "Mar", "Abr", "May", "Jun", "Jul", "Ago", "Sep", "Oct", "Nov", "Dic"];

        THIS.humanDate = function (date){
        	var result = "";
        	if(THIS.isToday(date)){
        		var hours = date.getHours();
        		var minutes = date.getMinutes();

        		var suffix = "am";
        		if(hours >= 12){ suffix = "pm"; }
        		if(hours >= 13){ hours -= 12; }
        		if(minutes < 10){ minutes = "0" + minutes.toString(); }

        		result = hours + ":" + minutes.toString() + " " + suffix;
        	}else{
            	var month = THIS.MONTHS[date.getMonth()];
            	result = date.getDate() + " " + month;
        	}
        	return result;
        }

        THIS.isToday = function(date){
        	var today = new Date();
        	return today.getDate() === date.getDate() &&
        			today.getFullYear() === date.getFullYear() &&
					today.getMonth()  === date.getMonth();
        }

        THIS.addRnd = function (str){
            return str + "?v=" +  Math.random().toString();
        }

        THIS.isValidDate = function (d) {
            return d instanceof Date && !isNaN(d);
        }

        //convert date object from server to date javscript
        THIS.toDate = function (obj){
            return new Date(
                obj.year,
                obj.monthValue-1,
                obj.dayOfMonth,
                obj.hour,
                obj.minute,
                0);
        }
})(Utils);
