package com.pw.droplet.braintree;


import java.util.Map;
import java.util.HashMap;

import android.util.Log;

import com.braintreepayments.api.interfaces.BraintreeCancelListener;
import com.braintreepayments.api.interfaces.ConfigurationListener;
import com.braintreepayments.api.models.PayPalRequest;
import com.google.gson.Gson;

import android.content.Intent;
import android.content.Context;
import android.app.Activity;


import androidx.appcompat.app.AppCompatActivity;

import com.braintreepayments.api.models.PaymentMethodNonce;
import com.braintreepayments.api.BraintreeFragment;
import com.braintreepayments.api.exceptions.InvalidArgumentException;
import com.braintreepayments.api.exceptions.BraintreeError;
import com.braintreepayments.api.exceptions.ErrorWithResponse;
import com.braintreepayments.api.models.CardBuilder;
import com.braintreepayments.api.Card;
import com.braintreepayments.api.PayPal;
import com.braintreepayments.api.interfaces.PaymentMethodNonceCreatedListener;
import com.braintreepayments.api.interfaces.BraintreeErrorListener;
import com.braintreepayments.api.models.CardNonce;
import com.braintreepayments.api.interfaces.BraintreeResponseListener;
import com.braintreepayments.api.models.Configuration;

import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ActivityEventListener;
import com.facebook.react.bridge.ReadableMap;


public class Braintree extends ReactContextBaseJavaModule implements ActivityEventListener, ConfigurationListener{
    private static final int PAYMENT_REQUEST = 1706816330;
    private static final int DEVICEID_REQUEST = 185392392;
    private String token;

    private Callback successCallback;
    private Callback errorCallback;


    private Callback deviceDataCallback;

    private Boolean collectDeviceData = false;

    private Context mActivityContext;

    private BraintreeFragment mBraintreeFragment;

    private ReadableMap threeDSecureOptions;

    public Braintree(ReactApplicationContext reactContext) {
        super(reactContext);
        reactContext.addActivityEventListener(this);
    }

    @Override
    public String getName() {
        return "Braintree";
    }

    public String getToken() {
        return this.token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    @ReactMethod
    public void setup(final String token, final Callback successCallback, final Callback errorCallback) {
        Log.d("Fragment setup", token);
        try {
            AppCompatActivity currentActivity = (AppCompatActivity) getCurrentActivity();
            this.mBraintreeFragment = BraintreeFragment.newInstance(currentActivity, token);

            this.mBraintreeFragment.addListener(new BraintreeCancelListener() {
                @Override
                public void onCancel(int requestCode) {
                    nonceErrorCallback("USER_CANCELLATION");
                }
            });
            this.mBraintreeFragment.addListener(new PaymentMethodNonceCreatedListener() {
                @Override
                public void onPaymentMethodNonceCreated(PaymentMethodNonce paymentMethodNonce) {
                    if (threeDSecureOptions != null && paymentMethodNonce instanceof CardNonce) {
                        CardNonce cardNonce = (CardNonce) paymentMethodNonce;
                        if (!cardNonce.getThreeDSecureInfo().isLiabilityShiftPossible()) {
                            nonceErrorCallback("3DSECURE_NOT_ABLE_TO_SHIFT_LIABILITY");
                        } else if (!cardNonce.getThreeDSecureInfo().isLiabilityShifted()) {
                            nonceErrorCallback("3DSECURE_LIABILITY_NOT_SHIFTED");
                        } else {
                            nonceCallback(paymentMethodNonce.getNonce());
                        }
                    } else {
                        nonceCallback(paymentMethodNonce.getNonce());
                    }
                }
            });
            this.mBraintreeFragment.addListener(new BraintreeErrorListener() {
                @Override
                public void onError(Exception error) {
                    Gson gson = new Gson();

                    Log.d("Errors", gson.toJson(error));
                    if (error instanceof ErrorWithResponse) {
                        ErrorWithResponse errorWithResponse = (ErrorWithResponse) error;
                        BraintreeError cardErrors = errorWithResponse.errorFor("creditCard");
                        if (cardErrors != null) {
                            Log.d("cardErrors != null:", gson.toJson(cardErrors)); 
                            // Gson gson = new Gson();
                            final Map<String, String> errors = new HashMap<>();
                            BraintreeError numberError = cardErrors.errorFor("number");
                            BraintreeError cvvError = cardErrors.errorFor("cvv");
                            BraintreeError expirationDateError = cardErrors.errorFor("expirationDate");
                            BraintreeError postalCode = cardErrors.errorFor("postalCode");
                            BraintreeError base = cardErrors.errorFor("base");

                            if (numberError != null) {
                                errors.put("card_number", numberError.getMessage());
                            }

                            if (cvvError != null) {
                                errors.put("cvv", cvvError.getMessage());
                            }

                            if (expirationDateError != null) {
                                errors.put("expiration_date", expirationDateError.getMessage());
                            }

                            if (base != null) {
                                errors.put("base", base.getMessage());
                            }

                            // TODO add more fields
                            if (postalCode != null) {
                                errors.put("postal_code", postalCode.getMessage());
                            }

                            nonceErrorCallback(gson.toJson(errors));
                        } else {
                            Log.d("errorWithResponse", gson.toJson(errorWithResponse));

                            nonceErrorCallback(errorWithResponse.getErrorResponse());
                        }
                    }
                }
            });
            this.setToken(token);
            successCallback.invoke(this.getToken());
        } catch (InvalidArgumentException e) {
            errorCallback.invoke(e.getMessage());
        }
    }

    @ReactMethod
    public void getCardNonce(final ReadableMap parameters, final Callback successCallback,
            final Callback errorCallback) {
        this.successCallback = successCallback;
        this.errorCallback = errorCallback;

        CardBuilder cardBuilder = new CardBuilder().validate(true);

        if (parameters.hasKey("number"))
            cardBuilder.cardNumber(parameters.getString("number"));

        if (parameters.hasKey("cvv"))
            cardBuilder.cvv(parameters.getString("cvv"));

        // In order to keep compatibility with iOS implementation, do not accept
        // expirationMonth and exporationYear,
        // accept rather expirationDate (which is combination of
        // expirationMonth/expirationYear)
        if (parameters.hasKey("expirationDate"))
            cardBuilder.expirationDate(parameters.getString("expirationDate"));

        if (parameters.hasKey("cardholderName"))
            cardBuilder.cardholderName(parameters.getString("cardholderName"));

        if (parameters.hasKey("firstName"))
            cardBuilder.firstName(parameters.getString("firstName"));

        if (parameters.hasKey("lastName"))
            cardBuilder.lastName(parameters.getString("lastName"));

        if (parameters.hasKey("company"))
            cardBuilder.company(parameters.getString("company"));

        /*if (parameters.hasKey("countryName"))
            cardBuilder.countryName(parameters.getString("countryName"));

        if (parameters.hasKey("countryCodeAlpha2"))
            cardBuilder.countryCodeAlpha2(parameters.getString("countryCodeAlpha2"));

        if (parameters.hasKey("countryCodeAlpha3"))
            cardBuilder.countryCodeAlpha3(parameters.getString("countryCodeAlpha3"));

        if (parameters.hasKey("countryCodeNumeric"))
            cardBuilder.countryCodeNumeric(parameters.getString("countryCodeNumeric"));

         */

        if (parameters.hasKey("locality"))
            cardBuilder.locality(parameters.getString("locality"));

        if (parameters.hasKey("postalCode"))
            cardBuilder.postalCode(parameters.getString("postalCode"));

        if (parameters.hasKey("region"))
            cardBuilder.region(parameters.getString("region"));

        if (parameters.hasKey("streetAddress"))
            cardBuilder.streetAddress(parameters.getString("streetAddress"));

        if (parameters.hasKey("extendedAddress"))
            cardBuilder.extendedAddress(parameters.getString("extendedAddress"));

        Card.tokenize(this.mBraintreeFragment, cardBuilder);
    }

    public void nonceCallback(String nonce) {
        this.successCallback.invoke(nonce);
    }

    public void nonceErrorCallback(String error) {
        this.errorCallback.invoke(error);
    }

    @ReactMethod
    public void paypalRequest(final Callback successCallback, final Callback errorCallback) {
        this.successCallback = successCallback;
        this.errorCallback = errorCallback;
        PayPalRequest request = new PayPalRequest();
        PayPal.requestBillingAgreement(this.mBraintreeFragment, request);
    }

    @Override
    public void onConfigurationFetched(Configuration configuration) {
        Log.d("Got configuration", configuration.toString());
    }


    @Override
    public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {

    }

    public void onNewIntent(Intent intent) {
    }
}
