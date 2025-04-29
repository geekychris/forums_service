// User related interfaces
export interface User {
  id: number;
  username: string;
  email: string;
  displayName?: string;
  createdAt: string;
  updatedAt: string;
}

// Forum related interfaces
export interface Forum {
  id: number;
  name: string;
  description: string;
  createdAt: string;
  updatedAt: string;
  createdBy: User;
  postCount?: number;
}

// Post related interfaces
export interface Post {
  id: number;
  title: string;
  content: string;
  createdAt: string;
  updatedAt: string;
  author: User;
  forum: Forum;
  commentCount?: number;
}

// Comment related interfaces
export interface Comment {
  id: number;
  content: string;
  createdAt: string;
  updatedAt: string;
  author: User;
  post: Post;
  parentCommentId?: number;
  replies?: Comment[];
}

// Authentication request/response interfaces
export interface LoginRequest {
  username: string;
  password: string;
}

export interface RegisterRequest {
  username: string;
  email: string;
  password: string;
  displayName?: string;
}

export interface AuthResponse {
  // Support both token naming conventions
  token?: string;
  accessToken?: string;
  
  // User information fields (may be directly in response or in a user object)
  id?: number;
  username?: string;
  email?: string;
  displayName?: string;
  role?: string;
  
  // Original user field for backward compatibility
  user?: User;
}

// Utility functions for AuthResponse
export const authUtils = {
  // Get token from different possible field names
  getToken: (auth: AuthResponse): string => {
    return auth.token || auth.accessToken || '';
  },
  
  // Convert auth response to User object
  toUser: (auth: AuthResponse): User => {
    if (auth.user) {
      return auth.user;
    }
    
    // If user is not provided but we have direct fields, construct a User
    if (auth.id !== undefined && auth.username !== undefined) {
      return {
        id: auth.id,
        username: auth.username,
        email: auth.email || '',
        displayName: auth.displayName || auth.username,
        // Use defaults for missing fields
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString()
      };
    }
    
    // Return minimal user if not enough information
    return {
      id: auth.id || 0,
      username: auth.username || 'user',
      email: auth.email || '',
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString()
    };
  }
};

// Creation request interfaces
export interface CreateForumRequest {
  name: string;
  description: string;
}

export interface CreatePostRequest {
  title: string;
  content: string;
  forumId: number;
}

export interface CreateCommentRequest {
  content: string;
  postId: number;
  parentCommentId?: number;
}

// Pagination response interface
export interface PageResponse<T> {
  content: T[];
  totalPages: number;
  totalElements: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
  empty: boolean;
}
