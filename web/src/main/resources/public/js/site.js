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

function ESC(ansicode) {
  return '\u001B[' + ansicode;
}

hterm.defaultStorage = new lib.Storage.Local();
var t = new hterm.Terminal();
t.onTerminalReady = function() {
  var maxLineLength = 256;
  var io = t.io.push();
  var input = '';
  var pos = 0;
  var currHist = 0;
  var history = [];
  var insertMode = true;

  io.onReadline = function(str) {
    busy(true);
    sendCommand(input, function(err, result) {
      t.io.print((result || err).replaceAll('\n', '\r\n'));
      busy(false);
    });
  };

  io.onVTKeystroke = io.sendString = function(str) {

    // TODO - prevent input when busy
    var chr = str.charCodeAt(0);
    if (chr === 13) {

      if (history.indexOf(input) < 0) {
        history.push(input);
        currHist = history.length;
      }
      io.onReadline(input);
      io.println('');
      input = '';
      pos = 0;

    } else if (chr === 127) {
      input = input.deleteCharAt(pos)
      pos -= 1;

    } else if (str == ESC('A') && currHist > 0) {
      currHist -= 1;
      input = history[currHist];
      pos = 0;

    } else if (str == ESC('B') && currHist < history.length - 1) {
      currHist += 1;
      input = history[currHist];
      pos = 0;

    } else if (str === ESC('C') && pos < input.length) { // forward
      pos += 1;

    } else if (str === ESC('D') && pos > 0) { // backwards
      pos -= 1;

    } else if (chr >= 32 && chr <= 127 && pos < maxLineLength) {
      if (insertMode) {
        input = input.insertCharAt(pos, str);
        pos += 1;
      } else {
        // TODO - replace mode
      }
    }

    io.print('\r' + ESC('K') + input + '\r');
    if (pos > 0) {
      io.print(ESC(pos + 'C'));
    }
  };

  sendCommand('', function(err, result) {
    t.io.print((result || err).replaceAll('\n', '\r\n'));
    busy(false);
  });
};

t.decorate(document.querySelector('#terminal'));
t.installKeyboard();