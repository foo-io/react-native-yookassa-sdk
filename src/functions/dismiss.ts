import { YookassaSdk } from '../YooKassaSdk';
import { Platform } from 'react-native';

export function dismiss() {
  if (Platform.OS === 'ios') {
    YookassaSdk.dismiss();
  }
}
