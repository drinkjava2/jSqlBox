if (typeof String.prototype.contains != 'function') {
  String.prototype.contains = function (str) {
    return this.indexOf(str) != -1;
  };
}

var callbackWithAjaxRedirect = function(realCallback) {
  return function() {
    var jqXHR = arguments[2];
    if (jqXHR.status == 278) {
      window.location = jqXHR.getResponseHeader("Location");
    }
    if (realCallback) realCallback.apply(this, arguments);
  }
}

jQuery.each(["get", "post", "put", "delete" ], function (i, method) {
  jQuery[ method ] = function (url, data, callback, type) {
    // shift arguments if data argument was omitted
    if (jQuery.isFunction(data)) {
      type = type || callback;
      callback = data;
      data = undefined;
    }

    if (method == "put") {
      var hasParam = url.contains("?");
      if (!hasParam) url = url + "?___=___";
      if (data) $.each(data, function(key, val){
        url = url + "&" + encodeURIComponent(key) + "=" + encodeURIComponent(val);
      });
      data = {};
    }

    return jQuery.ajax({
      url: url,
      type: method,
      dataType: type,
      data: data,
      success: callbackWithAjaxRedirect(callback)
    });
  };
});

jQuery.each(["getJSON", "postJSON", "putJSON", "deleteJSON"], function (i, method) {
  jQuery[ method ] = function (url, data, callback) {
    if (jQuery.isFunction(data)) {
      callback = data;
      data = undefined;
    }

    if (method.startsWith("put")) {
      var hasParam = url.contains("?");
      if (!hasParam) url = url + "?___=___";
      if (data) $.each(data, function(key, val){
        url = url + "&" + encodeURIComponent(key) + "=" + encodeURIComponent(val);
      });
      data = {};
    }
    if (method.startsWith("get")) {
      var hasParam = url.contains("?");
      if (!hasParam)
      {
        url = url +"?now="+ new Date().getTime();
      }
      else
      {
        url = url +"&now="+ new Date().getTime();
      }
    }
    return jQuery.ajax({
      url: url,
      type: method.replace("JSON", ""),
      dataType: "json",
      data: data,
      success: callbackWithAjaxRedirect(callback)
    });
  };
});
