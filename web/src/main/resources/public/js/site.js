"use strict";

String.prototype.replaceAll = function(search, replacement) {
  var target = this;
  return target.replace(new RegExp(search, 'g'), replacement);
};

String.prototype.deleteCharAt = function(pos) {
  var target = this;
  return (pos > 0) ? target.substring(0, pos - 1) + target.substring(pos) : target;
};

String.prototype.insertCharAt = function(pos, char) {
  var target = this;
  return target.substring(0, pos) + char + target.substring(pos);
};


function busy(enable) {
  var elem = document.getElementById('busy')
  elem.className = enable ? 'blink' : 'hide';
}

function sendCommand(text, cb) {
  var xhr = new XMLHttpRequest();

  xhr.open('POST', '/byok3', true);
  xhr.setRequestHeader('Accept', 'text/plain');
  xhr.setRequestHeader('Content-type', 'application/x-www-form-urlencoded');
  xhr.onload = function() {
    cb(null, xhr.responseText);
  };
  xhr.onerror = function(e) {
    cb("Unknown error occured: server response not received.", null);
  };

  xhr.send(text);
}

hterm.defaultStorage = new lib.Storage.Local();
var t = new hterm.Terminal();
t.onTerminalReady = function() {
  var io = t.io.push();
  var input = '';
  var pos = 0;
  var history = [];
  var ESC = '\u001B[';

  io.onReadline = function(str) {
    busy(true);
    sendCommand(input, function(err, result) {
      t.io.print((result || err).replaceAll('\n', '\r\n'));
      busy(false);
    });
  };

  io.onVTKeystroke = io.sendString = function(str) {
    var chr = str.charCodeAt(0);
    if (chr === 13) {
      history.push(input);
      io.onReadline(input);
      io.println('');
      input = '';
      pos = 0;
    } else if (chr === 127) {
      input = input.deleteCharAt(pos)
      pos -= 1;
      io.print('\b');
    } else if (str === ESC + '[C' && pos < input.length) { // forward
      io.print(ESC + '[1C');
      pos += 1;
    } else if (str === ESC + '[D' && pos > 0) { // backwards
      io.print(ESC + '[1D');
      pos -= 1;
    } else if (chr >= 32 && chr <= 127) {
      console.log(chr);
      t.io.print(str);
      input = input.insertCharAt(pos, str);
      pos += 1;
    }
  };

  sendCommand('', function(err, result) {
    t.io.print((result || err).replaceAll('\n', '\r\n'));
    busy(false);
  });
};

t.decorate(document.querySelector('#terminal'));
t.installKeyboard();