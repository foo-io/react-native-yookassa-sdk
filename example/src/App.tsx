import * as React from 'react';

import { StyleSheet, View } from 'react-native';
// import {
//   Currency,
//   PaymentMethodType,
//   SavePaymentMethod,
//   startTokenize,
// } from 'react-native-yookassa-sdk';

export default function App() {
  //const [result, setResult] = React.useState<number | undefined>();

  // React.useEffect(() => {
  //   startTokenize({
  //     amount: { value: 100, currency: Currency.RUB },
  //     title: 'Заголовок',
  //     subtitle: 'Подзаголовок',
  //     clientApplicationKey: 'test_NjY3MTY3NS1m6nMF5kqKGyk8ItcdVWx-Qoz7IWOta6U',
  //     shopId: '667167',
  //     savePaymentMethod: SavePaymentMethod.OFF,
  //     paymentMethodTypes: [
  //       PaymentMethodType.SBERBANK,
  //       PaymentMethodType.BANK_CARD,
  //       PaymentMethodType.GOOGLE_PAY,
  //       PaymentMethodType.YOO_MONEY,
  //     ],
  //   }).then((r) => {
  //     debugger
  //   }).catch((e) => {
  //     debugger
  //   });
  // }, []);

  return <View style={styles.container} />;
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
  },
  box: {
    width: 60,
    height: 60,
    marginVertical: 20,
  },
});
