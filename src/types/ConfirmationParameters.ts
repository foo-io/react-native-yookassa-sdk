import type { PaymentMethodType } from './PaymentMethodType';

export type ConfirmationParameters = {
  shopId: string;
  confirmationUrl: string;
  paymentMethodType: PaymentMethodType;
  clientApplicationKey: string;
};
