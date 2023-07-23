import Foundation

import YooKassaPayments

@objc(YookassaSdk)
class YookassaSdk: NSObject, TokenizationModuleOutput {

    private var viewController: UIViewController?
    private var resolvePromise: RCTPromiseResolveBlock?
    private var rejectPromise: RCTPromiseRejectBlock?
    private var promiseFulfilled: Bool = false;

    func didFinish(on module: YooKassaPayments.TokenizationModuleInput, with error: YooKassaPayments.YooKassaPaymentsError?) {

        dismiss()

        if error == nil {
            self.resolvePromise?([
              "result": "CANCELED"
            ])
        } else {
            let e = NSError(domain: "", code: 0, userInfo: [NSLocalizedDescriptionKey: "ERROR"])

            self.rejectPromise?(
                "Произошла",
                "ошибка",
                e
            )
        }

        self.resolvePromise = nil
        self.rejectPromise = nil
    }

    func didFinishConfirmation(paymentMethodType: YooKassaPayments.PaymentMethodType) {
        let result: NSDictionary = [
            "result": "FINISHED",
            "paymentMethodType" : paymentMethodType.rawValue.uppercased()
        ]

        dismiss()

        self.resolvePromise?(result)

        self.resolvePromise = nil
        self.rejectPromise = nil
    }

    func tokenizationModule(_ module: YooKassaPayments.TokenizationModuleInput, didTokenize token: YooKassaPayments.Tokens, paymentMethodType: YooKassaPayments.PaymentMethodType) {

        self.resolvePromise?([
          "result": "OK",
          "paymentToken": token.paymentToken,
          "paymentMethodType": paymentMethodType.rawValue
        ])

        self.resolvePromise = nil
        self.rejectPromise = nil
    }

    func getCurrency(currency: String) -> Currency {
        switch currency {
        case "RUB":
            return .rub
        case "USD":
            return .usd
        case "EUR":
            return .eur
        default:
            return .rub
        }
    }

    func getSavePaymentMethod(savePaymentMethod: String) -> SavePaymentMethod {
        switch (savePaymentMethod) {
        case "OFF":
            return SavePaymentMethod.off
        case "ON":
            return SavePaymentMethod.on
        case "USERS_SELECTS":
            return SavePaymentMethod.userSelects
        default:
            return SavePaymentMethod.userSelects
        }
    }

    func hexToUIColor(hex: String, alpha: CGFloat = 1.0) -> UIColor! {
        var hexFormatted: String = hex.trimmingCharacters(in: CharacterSet.whitespacesAndNewlines).uppercased()

        if hexFormatted.hasPrefix("#") {
            hexFormatted = String(hexFormatted.dropFirst())
        }

        //assert(hexFormatted.count == 6, "Invalid hex code used.")

        var rgbValue: UInt64 = 0

        Scanner(string: hexFormatted).scanHexInt64(&rgbValue)

        return UIColor(red: CGFloat((rgbValue & 0xFF0000) >> 16) / 255.0,
                       green: CGFloat((rgbValue & 0x00FF00) >> 8) / 255.0,
                       blue: CGFloat(rgbValue & 0x0000FF) / 255.0,
                       alpha: alpha)
    }

    func getPaymentMethodTypes(paymentTypes: [String]?,
                               authCenterClientId: String?,
                               applePayMerchantId: String?) -> PaymentMethodTypes {
        var paymentMethodTypes: PaymentMethodTypes = []

        if (paymentTypes != nil) {
            paymentTypes!.forEach { type in
                if let payType = PaymentMethodType(rawValue: type.lowercased()) {
                    if (payType == .yooMoney && authCenterClientId == nil) {
                        return
                    }

                    if (payType == .applePay && applePayMerchantId == nil) {
                        return
                    }

                    paymentMethodTypes.insert(PaymentMethodTypes(rawValue: [payType]))
                }
            }
        } else {
            paymentMethodTypes.insert(.bankCard)
            paymentMethodTypes.insert(.sberbank)

            if (authCenterClientId != nil) {
                paymentMethodTypes.insert(.yooMoney)
            }

            if (applePayMerchantId != nil) {
                paymentMethodTypes.insert(.applePay)
            }
        }

        return paymentMethodTypes
    }

    @objc(startConfirmation:withResolver:withRejector:)
    func startConfirmation(_ params: NSDictionary,
                           resolver resolve: @escaping RCTPromiseResolveBlock,
                           rejecter reject: @escaping RCTPromiseRejectBlock) -> Void {

        guard let confirmationUrl = params["confirmationUrl"] as? String,
              let _paymentMethodType = params["paymentMethodType"] as? String
        else {
            return
        }

        guard let paymentMethodType = PaymentMethodType(rawValue: _paymentMethodType.lowercased()),
              let viewController = viewController as? TokenizationModuleInput
        else {
            return
        }

        self.resolvePromise = resolve;
        self.rejectPromise = reject;

        viewController.startConfirmationProcess(
            confirmationUrl: confirmationUrl,
            paymentMethodType: paymentMethodType
        )
    }

    @objc(startTokenize:withResolver:withRejector:)
    func startTokenize(_ params: NSDictionary,
                       resolver resolve: @escaping RCTPromiseResolveBlock,
                       rejecter reject: @escaping RCTPromiseRejectBlock) -> Void  {

        self.resolvePromise = resolve;
        self.rejectPromise = reject;

        guard let clientApplicationKey = params["clientApplicationKey"] as? String,
              let shopName = params["title"] as? String,
              let purchaseDescription = params["subtitle"] as? String,
              let savePaymentMethod = params["savePaymentMethod"] as? String,
              let _amount = params["amount"] as? NSDictionary
        else {
            return
        }

        guard let amountValue = _amount["value"] as? NSNumber,
              let currency = _amount["currency"] as? String
        else {
            return
        }

        let currentCurrency = getCurrency(currency: currency)
        let amount = Amount(value: amountValue.decimalValue, currency: currentCurrency)

        let paymentTypes = params["paymentMethodTypes"] as? [String]
        let authCenterClientId = params["authCenterClientId"] as? String
        let applePayMerchantId = params["applePayMerchantId"] as? String
        let userPhoneNumber = params["userPhoneNumber"] as? String
        let customerId = params["customerId"] as? String
        let gatewayId = params["gatewayId"] as? String
        let returnUrl = params["returnUrl"] as? String
        let testMode = params["testMode"] as? Bool
        let showYooKassaLogo = params["showYooKassaLogo"] as? Bool
        let primaryColor = params["primaryColor"] as? String
        let applicationScheme = params["applicationScheme"] as? String

        let paymentMethodTypes = getPaymentMethodTypes(
            paymentTypes: paymentTypes,
            authCenterClientId: authCenterClientId,
            applePayMerchantId: applePayMerchantId
        )

        let tokenizationSettings = TokenizationSettings(
            paymentMethodTypes: paymentMethodTypes
        )

        let customizationSettings = CustomizationSettings(
            mainScheme: primaryColor != nil ? hexToUIColor(hex: primaryColor!) : CustomizationColors.blueRibbon,
            showYooKassaLogo: showYooKassaLogo == true || showYooKassaLogo == nil
        )

        let testModeSettings = TestModeSettings(paymentAuthorizationPassed: false,
                                                cardsCount: 2,
                                                charge: Amount(value: 100, currency: currentCurrency),
                                                enablePaymentError: false)

        let tokenizationModuleInputData =
            TokenizationModuleInputData(clientApplicationKey: clientApplicationKey,
            shopName: shopName,
            purchaseDescription: purchaseDescription,
            amount: amount,
            gatewayId: gatewayId,
            tokenizationSettings: tokenizationSettings,
            testModeSettings: testMode == true ? testModeSettings : nil,
            //cardScanning: CardScannerProvider(),
            applePayMerchantIdentifier: applePayMerchantId,
            returnUrl: returnUrl,
            isLoggingEnabled: testMode == true,
            userPhoneNumber: userPhoneNumber,
            customizationSettings: customizationSettings,
            savePaymentMethod: getSavePaymentMethod(savePaymentMethod: savePaymentMethod),
            moneyAuthClientId: authCenterClientId,
            applicationScheme: applicationScheme,
            customerId: customerId
        )

        DispatchQueue.main.async {
            let inputData: TokenizationFlow = .tokenization(tokenizationModuleInputData)
            self.viewController = TokenizationAssembly.makeModule(inputData: inputData, moduleOutput: self)
            let rootViewController = UIApplication.shared.keyWindow!.rootViewController!
            rootViewController.present(self.viewController!, animated: true, completion: nil)
        }
    }

    @objc
    func dismiss() {
        DispatchQueue.main.async { [weak self] in
            guard let self = self else { return }

            self.viewController?.dismiss(animated: true)
        }
    }
}

