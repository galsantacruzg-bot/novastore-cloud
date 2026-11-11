export interface OrderItemRequest {
  productId: number;
  quantity: number;
}

export interface CreateOrderRequest {
  customerId: number;
  items: OrderItemRequest[];
}

export interface GuestOrderRequest {
  name: string;
  email: string;
  phone: string;
  shippingAddress: string;
  items: OrderItemRequest[];
}

export interface OrderItem {
  id: number;
  productId: number;
  productName: string;
  quantity: number;
  unitPrice: number;
  subtotal: number;
}

export interface Order {
  id: number;
  customerId?: number;
  accountId?: number;
  customerName: string;
  customerEmail: string;
  customerPhone: string;
  shippingAddress: string;
  purchaseMode: string;
  total: number;
  status: string;
  createdAt: string;
  items: OrderItem[];
}
