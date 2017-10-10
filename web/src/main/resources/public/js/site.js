"use strict";

String.prototype.replaceAll = function(search, replacement) {
  var target = this;
  return target.replace(new RegExp(search, 'g'), replacement);
};

String.prototype.deleteCharAt = function(pos) {
  var target = this;
  return (pos > 0) ? target.substring(0, pos - 1) + target.substring(pos) : target;
};

String.prototype.insertAt = function(pos, str) {
  var target = this;
  return target.substring(0, pos) + str + target.substring(pos);
};

function busy(enable) {
  var elem = document.getElementById('busy')
  elem.className = enable ? 'blink' : 'hide';
}

function isBusy() {
  var elem = document.getElementById('busy')
  return elem.className === 'blink';
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
  var offset = 1;

  io.onReadline = function(str) {
    busy(true);
    sendCommand(input, function(err, result) {
      t.io.print(ESC('0G') + (result || err).replaceAll('\n', '\r\n'));
      offset = result.lastIndexOf('|  ') < 0 ? 1 : 4;
      busy(false);
    });
  };

  io.onVTKeystroke = io.sendString = function(str) {
    if (isBusy()) return;

    var chr = str.charCodeAt(0);
    if (chr === 13) {

      var histIdx = history.indexOf(input.trim());
      if (histIdx >= 0) {
        history.splice(histIdx, 1);
      }
      if (input.trim() !== "") {
        history.push(input.trim());
      }
      currHist = history.length;

      io.onReadline(input);
      io.println('');
      input = '';
      pos = 0;

    } else if (chr === 1) { // Ctrl-A
      pos = 0;

    } else if (chr === 5) { // Ctrl-E
      pos = input.length;

    } else if (chr === 127 && pos > 0) { // Backspace
      input = input.deleteCharAt(pos)
      pos -= 1;

    } else if (str === ESC("3~")) { // Delete
      input = input.deleteCharAt(pos + 1)

    } else if (str === ESC('A') && currHist > 0) { // Cursor up
      currHist -= 1;
      input = history[currHist];
      pos = input.length;

    } else if (str === ESC('B') && currHist < history.length) { // Cursor down
      currHist += 1;
      input = currHist < history.length ? history[currHist] : "";
      pos = input.length;

    } else if (str === ESC('C') && pos < input.length) { // Cursor right
      pos += 1;

    } else if (str === ESC('D') && pos > 0) { // Cursor left
      pos -= 1;

    } else if (chr >= 32 && chr < 127 && (pos + str.length) < maxLineLength) {
      if (insertMode) {
        input = input.insertAt(pos, str);
        pos += str.length;
      } else {
        // TODO - replace mode
      }
    }

    io.print(ESC(offset + 'G') + ESC('K') + input + ESC((offset + pos) + 'G'));
  };

  sendCommand('', function(err, result) {
    t.io.print((result || err).replaceAll('\n', '\r\n'));
    busy(false);
  });
};

t.decorate(document.querySelector('#terminal'));
t.installKeyboard();