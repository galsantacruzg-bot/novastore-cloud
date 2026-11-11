export interface AuthSession {
  token: string;
  userId: number;
  username: string;
  email: string;
  role: 'ADMIN' | 'USER';
}

export interface RegisterRequest {
  username: string;
  email: string;
  password: string;
}
