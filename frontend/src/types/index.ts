// Data model interfaces

export interface User {
  id: number;
  username: string;
  email: string;
  displayName: string;
}

export interface Forum {
  id: number;
  name: string;
  description: string;
  createdAt: string;
  updatedAt: string;
  posts?: Post[];
}

export interface Post {
  id: number;
  title: string;
  content: string;
  author: User;
  forum: Forum;
  createdAt: string;
  updatedAt: string;
  comments?: Comment[];
}

export interface Comment {
  id: number;
  content: string;
  author: User;
  post: Post;
  parentComment?: Comment;
  replies?: Comment[];
  createdAt: string;
  updatedAt?: string;
}

export interface Content {
  id: number;
  type: string;
  url: string;
  filename: string;
  fileSize: number;
  post?: Post;
  comment?: Comment;
  createdAt: string;
}

// API request/response interfaces

export interface LoginRequest {
  username: string;
  password: string;
}

export interface RegisterRequest {
  username: string;
  email: string;
  password: string;
  displayName: string;
}

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

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  hasNext: boolean;
  hasPrevious: boolean;
}

export interface AuthResponse {
  token: string;
  user: User;
}

// App state types

export interface AuthState {
  user: User | null;
  token: string | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  error: string | null;
}

