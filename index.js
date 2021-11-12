'use strict';

import { NativeModules, processColor, Platform } from 'react-native';
import { mapParameters } from './utils';

const Braintree = NativeModules.Braintree;

module.exports = {
  setupWithURLScheme(token, urlscheme) {
    return new Promise(function(resolve, reject) {
      if (Platform.OS === 'ios') {
          Braintree.setupWithURLScheme(token, urlscheme, function(success) {
            success == true ? resolve(true) : reject("Invalid Token");
          });
      } else {
          reject('setupWithURLScheme is only available on ios devices');
      }
    });
  },
  setup(token) {
    return new Promise(function(resolve, reject) {
      Braintree.setup(token, test => resolve(test), err => reject(err));
    });
  },

  getCardNonce(parameters = {}) {
    return new Promise(function(resolve, reject) {
      if (Platform.OS === 'ios') {
          try {
              resolve(Braintree.getCardNonce(mapParameters(parameters)));
          } catch (error) {
              reject(error);
          }
      } else {
          Braintree.getCardNonce(
            mapParameters(parameters),
            nonce => resolve(nonce),
            err => reject(err)
          );
      }

    });
  },

  showPaymentViewController(config = {}) {
    var options = {
      tintColor: Platform.OS === 'ios' ? processColor(config.tintColor) :config.tintColor,
      bgColor: Platform.OS === 'ios' ? processColor(config.bgColor) : config.bgColor,
      barBgColor: Platform.OS === 'ios' ? processColor(config.barBgColor) : config.barBgColor,
      barTintColor: Platform.OS === 'ios' ? processColor(config.barTintColor): config.barTintColor,
      callToActionText: config.callToActionText,
      title: config.title,
      description: config.description,
      amount: config.amount,
    };

    if (Platform.OS === 'ios') {
        return new Promise(function(resolve, reject) {
            Braintree.showPaymentViewController(options, function(err, nonce) {
                nonce != null ? resolve(nonce) : reject(err);
            });
        });
    } else {
        return new Promise(function(resolve, reject) {
          Braintree.paymentRequest(
            options,
            nonce => resolve(nonce),
            error => reject(error)
          );
        });
    }
  },

  showPayPalViewController() {
    return new Promise(function(resolve, reject) {
      if (Platform.OS === 'ios') {
          Braintree.showPayPalViewController(function(err, nonce) {
            nonce != null ? resolve(nonce) : reject(err);
          });
      } else {
          Braintree.paypalRequest(nonce => resolve(nonce), error => reject(error));
      }
    });
  },
  showApplePayViewController(options = {}) {
    return new Promise(function(resolve, reject) {
      if (Platform.OS === 'ios') {
          Braintree.showApplePayViewController(options, function(err, nonce) {
            nonce != null ? resolve(nonce) : reject(err);
          });
      } else {
         reject('showApplePayViewController is only available on ios devices');
      }
    });
  },
};
