import type { PaymentMethodType } from './';

export type ConfirmationResult = {
  result: string;
  paymentMethodType: PaymentMethodType;
};
