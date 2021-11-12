//
//  RCTBraintree.h
//  RCTBraintree
//
//  Created by Rickard Ekman on 18/06/16.
//  Copyright © 2016 Rickard Ekman. All rights reserved.
//

#import <React/RCTBridgeModule.h>
#import <React/RCTUtils.h>
#import <React/RCTConvert.h>

#import "BraintreeCore.h"
#import "BraintreePayPal.h"
#import "BraintreeApplePay.h"
#import "BraintreeCard.h"
#import "BraintreeUI.h"
#import "PPDataCollector.h"

@import PassKit;

@interface RCTBraintree : UIViewController <RCTBridgeModule, BTDropInViewControllerDelegate, BTViewControllerPresentingDelegate, PKPaymentAuthorizationViewControllerDelegate>

@property (nonatomic, strong) BTAPIClient *braintreeClient;
@property (nonatomic, strong) UIViewController *reactRoot;

// ApplePay
@property (nonatomic, strong) PKPaymentRequest *paymentRequest;
@property (nonatomic, strong) NSString *applePayNonce;

@property (nonatomic, strong) RCTResponseSenderBlock callback;

+ (instancetype)sharedInstance;
- (BOOL)application:(UIApplication *)application openURL:(NSURL *)url;

@end
