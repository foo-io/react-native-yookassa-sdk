import { YookassaSdk } from '../YooKassaSdk';
import type { PaymentParameters } from '../types';
import type { TokenizationResult } from '../types/TokenizationResult';

export function startTokenize(
  paymentParameters: PaymentParameters
): Promise<TokenizationResult> {
  return YookassaSdk.startTokenize(paymentParameters);
}
