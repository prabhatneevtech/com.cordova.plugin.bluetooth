/*global cordova*/
module.exports = {

    connect: function (macAddress, success, failure) {
        cordova.exec(success, failure, "Bluetooth", "connect", [macAddress]);
    }

};
