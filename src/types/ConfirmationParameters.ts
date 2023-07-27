import type { PaymentMethodType } from './PaymentMethodType';

export type ConfirmationParameters = {
  confirmationUrl: string;
  paymentMethodType: PaymentMethodType;
  clientApplicationKey: string;
};
