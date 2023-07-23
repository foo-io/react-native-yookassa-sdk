import type { SavePaymentMethod } from './SavePaymentMethod';
import type { GooglePayParameters } from './GooglePayParameters';
import type { PaymentMethodType } from './PaymentMethodType';
import type { Amount } from './Amount';

export type PaymentParameters = {
  amount: Amount;
  title: string;
  subtitle: string;
  clientApplicationKey: string;
  shopId: string;
  savePaymentMethod: SavePaymentMethod;
  paymentMethodTypes?: PaymentMethodType[];
  gatewayId?: string;
  customReturnUrl?: string;
  userPhoneNumber?: string;
  googlePayParameters?: GooglePayParameters;
  authCenterClientId?: string;
  customerId?: string;
  testMode?: boolean;
  showYooKassaLogo?: boolean;
  primaryColor?: string;
  applicationScheme?: string;
};
