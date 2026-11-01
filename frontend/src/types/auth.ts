export interface AdminUser {
  id: number
  email: string
  role: 'ADMIN'
  mustChangePassword: boolean
  lastLoginAt: string | null
}
