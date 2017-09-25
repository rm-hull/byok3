"use strict";

var scrollback = document.getElementById("scrollback");
var input = document.getElementById("expression");

function addLine(text) {
  var elem = document.createElement("div");
  elem.className = "output";
  elem.innerHTML = text.replace(/</g,"&lt;").replace(/>/g,"&gt;") || " ";
  scrollback.appendChild(elem);
}

function clearScrollback() {
  var children = scrollback.childNodes;
  for (var i = 0, n = children.length; i < n; i++) {
    scrollback.removeChild(children[0]);
  }
}

function sendCommand(text, cb) {
  var xhr = new XMLHttpRequest();
  xhr.open('POST', '/byok3', true);
  xhr.setRequestHeader('Content-type', 'application/x-www-form-urlencoded');
  xhr.onload = function() {
    cb(this.responseText);
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
    sendCommand(input.value, function(result) {
      addLine(result);
      input.scrollIntoView({behaviour: "instant", block: "end"});
    });
    input.value = "";
  }
};

sendCommand("", addLine);