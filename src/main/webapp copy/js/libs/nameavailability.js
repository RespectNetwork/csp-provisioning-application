// Cloud name availiblity checker for jQuery
// Respect Network & The Trusted Cloud Company 
// Borrowed heavily from <https://github.com/narfdotpl/jquery-typing>

(function ($) {
   
   $.fn.checkCloud = function (opts) {
       
       // Default options
       var options = $.extend({
           delay: 400,                             // How long to wait after user has stopped typing
           availibilityAPI: "./api/cloud_names/",       // URL of cloud name restful API
           stripSymbol:false,
           completed: null,                        // Callback after name has been checked takes element and boolean. Must be set.
           changed: null                           // Callback after name has been changed takes element
       }, opts);

       var typing = false,
           $elem = $(this),
           validCloudFormat = /^=[a-z\d]+((\.|\-)[a-z\d]+)*$/, // Regex to validate cloud name format
           cloudNameRequest,
           cloudCheck,
           cloudName;
       
       // Test cloud name against regex
       function isValidFormat(cloudName) {
           if (cloudName < 3 ) {
               return false;
           }
           return validCloudFormat.test(cloudName);
       }
       
       // After user has stopped typing, check cloud name
       function checkCloudName(event, delay) {
           if (typeof delay === "undefined") {
               delay = options.delay;
           }
           
           if (typing) {
               // Clear old cloud check functions
               clearTimeout(cloudCheck);
               
               // Delay calling of cloud check
               cloudCheck = setTimeout(function () {
                   typing = false;
                   cloudName = $elem.val().toLowerCase();
                   if ( typeof options.completed === "function" ) {
                       if (isValidFormat(cloudName)) {
                       
                       		if(options.stripSymbol)
                       		{
                       			cloudName = cloudName.substr(1,100);
                       		}
                           cloudNameRequest = $.ajax({
                               url: options.availibilityAPI + cloudName,
                               dataType: "jsonp",
                               success: function(data) {
                                   options.completed.call(data,$elem, data.available);
                               }
                           });
                       }else{
                           options.completed.call($elem, false);
                       }
                   }
               }, delay);
           }
       }
       
       // When the cloud name has changed abort all calls to the API
       // and call call the changed callback.
       function cloudNameChanged() {
           if (!typing) {
               if ( cloudNameRequest ) {
                   cloudNameRequest.abort();
               }
               
               typing = true;
               
               if ( typeof options.changed === "function" ) {
                   options.changed.call($elem);
               }
           }
       }
       
       // Check cloud names on keyup
       $elem.keyup(checkCloudName);
       
       // Check cloud names without any delay on blur
        $elem.blur( function(event) {
            checkCloudName(event, 0);
        });
       
       // Change cloud name on keypress
       $elem.keypress(cloudNameChanged);
       
       // Track delete and enter events
       $elem.keydown(function (event) {
          if (event.keyCode === 8 || event.keyCode === 46) {
              cloudNameChanged();
          }
       });
   }
})(jQuery);
