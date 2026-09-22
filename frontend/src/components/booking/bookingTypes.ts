export interface BookingQuoteRequest {
  hallId: number;
  startAt: string; // ISO8601
  endAt: string;   // ISO8601
  guestCount: number;
}

export interface PriceBreakdown {
  basePrice: number;
  memberDiscount: number;
  subtotal: number;
  platformFee: number;
  tax: number;
  total: number;
  currency: string;
}

export interface BookingCreateRequest {
  idempotencyKey: string;
  hallId: number;
  eventType: string;
  eventTitle: string;
  themeTags: string[];
  guestCount: number;
  startAt: string;
  endAt: string;
}

export interface BookingResponse {
  publicId: string;
  bookingRef: string;
  hallId: number;
  renterUserId: number;
  eventType: string;
  eventTitle: string;
  guestCount: number;
  startAt: string;
  endAt: string;
  status: string;
  priceTotal: number;
  currency: string;
  cancellationPolicy: string;
  createdAt: string;
}
