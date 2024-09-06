package com.yookassasdk

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import androidx.annotation.NonNull
import com.facebook.react.bridge.*
import ru.yoomoney.sdk.kassa.payments.Checkout
import ru.yoomoney.sdk.kassa.payments.Checkout.createConfirmationIntent
import ru.yoomoney.sdk.kassa.payments.TokenizationResult
import ru.yoomoney.sdk.kassa.payments.checkoutParameters.*
import ru.yoomoney.sdk.kassa.payments.ui.color.ColorScheme
import java.math.BigDecimal
import java.util.*

class YookassaSdkModule(reactContext: ReactApplicationContext) :
  ReactContextBaseJavaModule(reactContext) {

  val REQUEST_CODE_TOKENIZE = 33
  val REQUEST_CODE_3DS = 34

  private var resultPromise: Promise? = null;

  private val activityEventListener: ActivityEventListener = object : BaseActivityEventListener() {
    override fun onActivityResult(activity: Activity, requestCode: Int, resultCode: Int, data: Intent?) {
      val result = Arguments.createMap()

      if (requestCode == REQUEST_CODE_TOKENIZE) {
        when (resultCode) {
          Activity.RESULT_OK -> {
            val tokenizationResult: TokenizationResult = Checkout.createTokenizationResult(data!!)

            result.putString("result", "OK")
            result.putString("paymentToken", tokenizationResult.paymentToken)
            result.putString("paymentMethodType", tokenizationResult.paymentMethodType.name)
          }
          Activity.RESULT_CANCELED -> // user canceled tokenization
            result.putString("result", "CANCELED")
        }
      }

      if (requestCode == REQUEST_CODE_3DS) {
        when (resultCode) {
          Activity.RESULT_OK ->
            result.putString("result", "FINISHED")
          Activity.RESULT_CANCELED ->
            result.putString("result", "CANCELED")
          Checkout.RESULT_ERROR ->
            result.putString("result", "ERROR")
        }
      }

      if (result.hasKey("result")) {
        when (result.getString("result")) {
          "OK", "FINISHED", "CANCELED" -> {
            resultPromise?.resolve(result)
          }
          "ERROR" -> {
            resultPromise?.reject("Произошла", "ошибка", result)
          }
        }
      }

      resultPromise = null
    }
  }

  @NonNull
  private fun getPaymentMethodTypes(paymentTypes: ReadableArray?, authCenterClientIdProvided: Boolean): Set<PaymentMethodType> {
    val paymentMethodTypes: MutableSet<PaymentMethodType> = HashSet()

    if (paymentTypes == null) {
      paymentMethodTypes.add(PaymentMethodType.BANK_CARD)
      paymentMethodTypes.add(PaymentMethodType.SBERBANK)
      paymentMethodTypes.add(PaymentMethodType.SBP)

      if (authCenterClientIdProvided) {
        paymentMethodTypes.add(PaymentMethodType.YOO_MONEY)
      }
    } else {
      for (i in 0 until paymentTypes.size()) {
        when (paymentTypes.getString(i)) {
          "BANK_CARD" -> paymentMethodTypes.add(PaymentMethodType.BANK_CARD)
          "SBERBANK" -> paymentMethodTypes.add(PaymentMethodType.SBERBANK)
          "SBP" -> paymentMethodTypes.add(PaymentMethodType.SBP)
          "YOO_MONEY" -> if (authCenterClientIdProvided) {
            paymentMethodTypes.add(PaymentMethodType.YOO_MONEY)
          }
        }
      }
    }

    return paymentMethodTypes
  }

  @NonNull
  private fun getGooglePaymentMethodTypes(googlePaymentTypes: ReadableArray?): Set<GooglePayCardNetwork> {
    val googlePaymentMethodTypes: MutableSet<GooglePayCardNetwork> = HashSet()

    if (googlePaymentTypes == null) {
      googlePaymentMethodTypes.add(GooglePayCardNetwork.AMEX)
      googlePaymentMethodTypes.add(GooglePayCardNetwork.DISCOVER)
      googlePaymentMethodTypes.add(GooglePayCardNetwork.JCB)
      googlePaymentMethodTypes.add(GooglePayCardNetwork.MASTERCARD)
      googlePaymentMethodTypes.add(GooglePayCardNetwork.VISA)
      googlePaymentMethodTypes.add(GooglePayCardNetwork.INTERAC)
      googlePaymentMethodTypes.add(GooglePayCardNetwork.OTHER)
    } else {
      for (i in 0 until googlePaymentTypes.size()) {
        when (googlePaymentTypes.getString(i)) {
          "AMEX" -> googlePaymentMethodTypes.add(GooglePayCardNetwork.AMEX)
          "DISCOVER" -> googlePaymentMethodTypes.add(GooglePayCardNetwork.DISCOVER)
          "JCB" -> googlePaymentMethodTypes.add(GooglePayCardNetwork.JCB)
          "MASTERCARD" -> googlePaymentMethodTypes.add(GooglePayCardNetwork.MASTERCARD)
          "VISA" -> googlePaymentMethodTypes.add(GooglePayCardNetwork.VISA)
          "INTERAC" -> googlePaymentMethodTypes.add(GooglePayCardNetwork.INTERAC)
          "OTHER" -> googlePaymentMethodTypes.add(GooglePayCardNetwork.OTHER)
        }
      }
    }

    return googlePaymentMethodTypes
  }

  @ReactMethod
  fun startTokenize(parameters: ReadableMap, promise: Promise) {
    resultPromise = promise

    val amount = parameters.getMap("amount")!!
    val amountValue = amount.getDouble("value")
    val currency = amount.getString("currency")
    val title = parameters.getString("title")!!
    val subtitle = parameters.getString("subtitle")!!
    val clientApplicationKey = parameters.getString("clientApplicationKey")!!
    val shopId = parameters.getString("shopId")!!
    val savePaymentMethod = parameters.getString("savePaymentMethod")!!
    val paymentMethods = if (parameters.hasKey("paymentMethodTypes")) parameters.getArray("paymentMethodTypes") else null
    val gatewayId = if (parameters.hasKey("gatewayId")) parameters.getString("gatewayId") else null
    val customReturnUrl = if (parameters.hasKey("customReturnUrl")) parameters.getString("customReturnUrl") else null
    val userPhoneNumber = if (parameters.hasKey("userPhoneNumber")) parameters.getString("userPhoneNumber") else null
    val googlePayParameters = if (parameters.hasKey("googlePayParameters")) parameters.getArray("googlePayParameters") else null
    val authCenterClientId = if (parameters.hasKey("authCenterClientId")) parameters.getString("authCenterClientId") else null
    val customerId = if (parameters.hasKey("customerId")) parameters.getString("customerId") else null
    val testMode = if (parameters.hasKey("testMode")) parameters.getBoolean("testMode") else null
    val primaryColor = if (parameters.hasKey("primaryColor")) parameters.getString("primaryColor") else null
    val showYooKassaLogo = if (parameters.hasKey("showYooKassaLogo")) parameters.getBoolean("showYooKassaLogo") else true

    val paymentMethodTypes = getPaymentMethodTypes(paymentMethods, authCenterClientId != null)
    val gPayParameters = getGooglePaymentMethodTypes(googlePayParameters)

    val testParameters = TestParameters(
      showLogs = true, // showLogs - включить/выключить отображение логов sdk
      googlePayTestEnvironment = true, // googlePayTestEnvironment - какую, тестовую или боевую, среду нужно использовать для Google Pay, подробнее можно почитать тут: https://developers.google.com/pay/api/android/guides/test-and-deploy/integration-checklist
      mockConfiguration = MockConfiguration(
        completeWithError = false, // completeWithError - возвращать всегда при токенизации ошибку
        paymentAuthPassed = true, // paymentAuthPassed - авторизован пользователь или нет, для оплаты кошельком
        linkedCardsCount = 3, // linkedCardsCount - количество карт, привязанных к кошельку пользователя;
        serviceFee = Amount(BigDecimal.ONE, Currency.getInstance("RUB")) // serviceFee - комиссия, которая будет отображена на экране выбранного способа оплаты
      )
    )

    val paymentParameters = PaymentParameters(
      amount = Amount(amountValue.toBigDecimal(), Currency.getInstance(currency)),
      title = title,
      subtitle = subtitle,
      clientApplicationKey = clientApplicationKey,
      shopId = shopId,
      savePaymentMethod = SavePaymentMethod.valueOf(savePaymentMethod),
      paymentMethodTypes = paymentMethodTypes,
      customReturnUrl = customReturnUrl,
      userPhoneNumber = userPhoneNumber,
      authCenterClientId = authCenterClientId,
      gatewayId = gatewayId,
      googlePayParameters = GooglePayParameters(gPayParameters),
      customerId = customerId,
    );

    val uiParameters = UiParameters(
      showLogo = showYooKassaLogo,
      colorScheme = if (primaryColor != null) ColorScheme(Color.parseColor(primaryColor)) else ColorScheme.getDefaultScheme()
    )

    val intent = if (testMode == null) Checkout.createTokenizeIntent(
      context = reactApplicationContext,
      paymentParameters = paymentParameters,
      uiParameters = uiParameters
    ) else Checkout.createTokenizeIntent(
      context = reactApplicationContext,
      paymentParameters = paymentParameters,
      uiParameters = uiParameters,
      testParameters = testParameters
    )

    val activity = currentActivity

    if (activity != null) {
      activity.startActivityForResult(intent, REQUEST_CODE_TOKENIZE)
    }
  }

  @ReactMethod
  fun startConfirmation(parameters: ReadableMap, promise: Promise) {
    resultPromise = promise;

    val confirmationUrl = parameters.getString("confirmationUrl")!!
    val shopId = parameters.getString("shopId")!!
    val clientApplicationKey = parameters.getString("clientApplicationKey")!!
    val paymentMethodType = PaymentMethodType.valueOf(parameters.getString("paymentMethodType")!!)
    val intent = createConfirmationIntent(reactApplicationContext, confirmationUrl, paymentMethodType, clientApplicationKey, shopId)

    if (currentActivity != null) {
      currentActivity?.startActivityForResult(intent, REQUEST_CODE_3DS)
    } else {
      resultPromise?.reject("", "Payment confirmation error.")
    }
  }

  init {
    reactContext.addActivityEventListener(activityEventListener)
  }

  override fun getName(): String {
    return NAME
  }

  companion object {
    const val NAME = "YookassaSdk"
  }
}
