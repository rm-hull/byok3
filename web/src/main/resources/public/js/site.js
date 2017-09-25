"use strict";

var scrollback = document.getElementById("scrollback");
var input = document.getElementById("expression");

function addLine(text) {
  var elem = document.createElement('div');
  elem.className = 'output';
  elem.innerHTML = (text || ' ').replace(/</g,'&lt;').replace(/>/g,'&gt;');
  scrollback.appendChild(elem);
}

function clearScrollback() {
  var children = scrollback.childNodes;
  for (var i = 0, n = children.length; i < n; i++) {
    scrollback.removeChild(children[0]);
  }
}

function busy(enable) {
  var elem = document.getElementById('busy')
  elem.className = enable ? 'blink' : 'hide';
}

function sendCommand(text, cb) {
  var xhr = new XMLHttpRequest();

  xhr.open('POST', '/byok3', true);
  xhr.setRequestHeader('Content-type', 'application/x-www-form-urlencoded');
  xhr.onload = function() {
    cb(null, xhr.responseText);
  };
  xhr.onerror = function(e) {
    cb("Unknown error occured: server response not received.", null);
  };

  xhr.send(text);
}

document.onclick = function(evt) {
  document.execCommand('copy');
  input.focus();
};

input.focus();
input.onkeypress = function(event) {
  if (event.which === 13) {
    addLine(input.value);
    busy(true);
    sendCommand(input.value, function(err, result) {
      addLine(result || err);
      input.scrollIntoView({behaviour: 'instant', block: 'end'});
      busy(false);
    });
    input.value = '';
    input.scrollIntoView({behaviour: 'instant', block: 'end'});
  }
};

sendCommand('', function(err, result) {
  addLine(result || err);
  busy(false);
});