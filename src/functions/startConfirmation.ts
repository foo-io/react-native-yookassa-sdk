import { YookassaSdk } from '../YooKassaSdk';
import type { ConfirmationParameters, ConfirmationResult } from '../types';

export function startConfirmation(
  confirmationParameters: ConfirmationParameters
): Promise<ConfirmationResult> {
  return YookassaSdk.startConfirmation(confirmationParameters);
}
