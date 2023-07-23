#import <React/RCTBridgeModule.h>

@interface RCT_EXTERN_MODULE(YookassaSdk, NSObject)

RCT_EXTERN_METHOD(startTokenize:(NSDictionary *) paymentParameters
                 withResolver:(RCTPromiseResolveBlock)resolve
                 withRejector:(RCTPromiseRejectBlock)reject)

RCT_EXTERN_METHOD(startConfirmation:(NSDictionary *) confirmationParameters
                  withResolver:(RCTPromiseResolveBlock)resolve
                  withRejector:(RCTPromiseRejectBlock)reject)

RCT_EXTERN_METHOD(dismiss)

+ (BOOL)requiresMainQueueSetup
{
  return NO;
}

@end
