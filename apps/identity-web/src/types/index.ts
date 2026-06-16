export interface UserProfile {
  id: number
  username: string
  email: string
  displayName: string
  roles: string[]
  permissions: string[]
}

export interface TokenResponse {
  accessToken: string
  refreshToken: string
  accessTokenExpiresAt: string
  user: UserProfile
}

export interface PageResult<T> {
  content: T[]
  page: number
  size: number
  totalElements: number
  totalPages: number
}

export interface ApiError {
  code: string
  message: string
  fieldErrors?: Record<string, string>
}
