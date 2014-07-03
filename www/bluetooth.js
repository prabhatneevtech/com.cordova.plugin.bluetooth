/*global cordova*/
var argscheck = require('cordova/argscheck'),
    exec = require('cordova/exec'),
    utils = require('cordova/utils')

module.exports = {


	getPairedDevice: function (success, failure) {
        //alert("hmm");
        cordova.exec(success, failure, "Bluetooth", "getPairedDevice", [""]);
    },
    startServer: function (success, failure) {
        cordova.exec(success, failure, "Bluetooth", "startServer", [""]);
    },

    connect: function (macAddress, success, failure) {
        cordova.exec(success, failure, "Bluetooth", "connect", [macAddress]);
    },
    write: function (message, success, failure) {
        cordova.exec(success, failure, "Bluetooth", "write", [message]);
    },
    read: function (success, failure) {
        cordova.exec(success, failure, "Bluetooth", "read", [""]);
    },
    isConnected: function (success, failure) {
        cordova.exec(success, failure, "Bluetooth", "isconnected", [""]);
    }

};
