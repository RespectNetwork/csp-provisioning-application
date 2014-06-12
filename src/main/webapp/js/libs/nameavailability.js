// Cloud name availiblity checker for jQuery
// Respect Network & The Trusted Cloud Company
// Borrowed heavily from <https://github.com/narfdotpl/jquery-typing>
(function ($) {

    $.fn.checkCloud = function (opts) {

        // Default options
        var options = $.extend({
            delay: 400,                             // How long to wait after user has stopped typing
            availabilityApi: "./api/cloud_names/",  // URL of cloud name restful API
            stripSymbol: false,
            completed: null,                        // Callback after name has been checked takes element and boolean. Must be set.
            changed: null                           // Callback after name has been changed takes element
        }, opts);

        var $elem = $(this),
            validCloudFormat = /^=[a-z\d]+((\.|\-)[a-z\d]+)*$/, // Regex to validate cloud name format
            cloudNameReq,
            cloudCheckTimeout,
            cloudName = '';

        // Test cloud name against regex
        function isValidFormat(cloudName) {
            if (cloudName.length < 3) {
                return false;
            }
            return validCloudFormat.test(cloudName);
        }

        // After user has stopped typing, check cloud name
        function checkCloudName(event, delay) {
            cloudName = event.data;

            // Delay calling of cloud check
            cloudCheckTimeout = setTimeout(function () {
                if (isValidFormat(cloudName)) {
                    var checkCloudName = cloudName;
                    if (options.stripSymbol && checkCloudName[0] == '=') {
                        checkCloudName = cloudName.substr(1, 100);
                    }

                    cloudNameReq = $.ajax({
                        url: options.availabilityApi + checkCloudName,
                        dataType: "jsonp",
                        success: function (data) {
                            callOnComplete(data.available);
                        }
                    });
                } else {
                    callOnComplete(false);
                }
            }, delay);
        }

        // hooke various input events
        $elem.on('change keydown keyup paste input blur', function (event) {
            var newCloudName = $elem.val().toLowerCase().trim();
            console.log('event.type=' + event.type + ', newCloudName=' + newCloudName + ', cloudName=' + cloudName);

            // deleted/cleared - don't add prefix, just show normal hint text
            if (newCloudName.length == 0) {
                cancelReq();
                callOnComplete(null);
                return;
            }

            // ensure input starts with =
            if (!newCloudName.startsWith('=')) {
                newCloudName = '=' + newCloudName;
                $elem.val(newCloudName);
                $elem.trigger(jQuery.Event(event.type));
                return;
            }

            // ignore if unchanged (e.g. from arrow keys or something)
            if (newCloudName == cloudName) {
                return;
            }

            // text has changed - cancel prev
            cancelReq();

            // call changed
            callOnChange();

            // check name
            event.data = newCloudName;
            var delay = (event.type == 'blur') ? 0 : options.delay; // no delay for blur event
            checkCloudName(event, delay);
        });

        function cancelReq() {
            if (cloudCheckTimeout) {
                clearTimeout(cloudCheckTimeout);
            }
            if (cloudNameReq) {
                cloudNameReq.abort();
            }
        }

        function callOnChange() {
            if (typeof options.changed === "function") {
                options.changed.call(this, $elem);
            }
        }

        function callOnComplete(result) {
            if (typeof options.completed === "function") {
                options.completed.call(this, $elem, result);
            } else {
                console.log('completed function undefined');
            }
        }

        $elem.trigger(jQuery.Event('change'));
    }
})(jQuery);
