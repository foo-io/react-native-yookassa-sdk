import type { PaymentMethodType } from './';

export type TokenizationResult = {
  result: string;
  paymentToken: string;
  paymentMethodType: PaymentMethodType;
};
