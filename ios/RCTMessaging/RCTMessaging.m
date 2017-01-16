//
//  RCTMessaging.m
//  RCTMessaging
//
//  Created by Jaune Sarmiento on 15/01/2017.
//  Copyright © 2017 Jaune Sarmiento. All rights reserved.
//

#import "RCTMessaging.h"
#import "RCTLog.h"

#import <MessageUI/MessageUI.h>

@interface RCTMessaging () <MFMessageComposeViewControllerDelegate>
@end

@implementation RCTMessaging
{
    UIViewController* _rootViewController;
    RCTPromiseResolveBlock _showResolver;
    RCTPromiseRejectBlock _showRejecter;
}

RCT_EXPORT_MODULE()

- (dispatch_queue_t)methodQueue
{
    return dispatch_get_main_queue();
}

RCT_EXPORT_METHOD(canSendText:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject)
{
    resolve([NSNumber numberWithBool:[MFMessageComposeViewController canSendText]]);
}

RCT_EXPORT_METHOD(sendText:(NSDictionary *)options resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject)
{
    _showResolver = resolve;
    _showRejecter = reject;
    
    MFMessageComposeViewController* composer = [[MFMessageComposeViewController alloc] init];
    composer.messageComposeDelegate = self;
    
    NSString* body = options[@"body"];
    NSArray* recipients = options[@"recipients"];
    
    if (body) {
        composer.body = body;
    }
    
    if (recipients) {
        composer.recipients = recipients;
    }
    
    _rootViewController = [UIApplication sharedApplication].keyWindow.rootViewController;
    [_rootViewController presentViewController:composer animated:YES completion:nil];
}

- (void)messageComposeViewController:(MFMessageComposeViewController *)controller didFinishWithResult:(MessageComposeResult)result
{
    [_rootViewController dismissViewControllerAnimated:YES completion:nil];
    
    if (result == MessageComposeResultSent) {
        _showResolver([NSNumber numberWithBool:NO]);
    } else if (result == MessageComposeResultCancelled) {
        _showResolver([NSNumber numberWithBool:YES]);
    } else {
        _showRejecter(@"RCTMessaging", @"Failed to send text message.", nil);
    }
    
    _showResolver = nil;
    _showRejecter = nil;
}

@end
