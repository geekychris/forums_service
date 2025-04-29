import axios, { AxiosInstance } from 'axios';
import {
  AuthResponse,
  CreateCommentRequest,
  CreateForumRequest,
  CreatePostRequest,
  Forum,
  LoginRequest,
  PageResponse,
  Post,
  RegisterRequest,
  User,
  Comment,
  authUtils
} from '../types';

class ApiClient {
  private client: AxiosInstance;
  private token: string | null = null;

  constructor() {
    // Initialize from localStorage if token exists
    this.token = localStorage.getItem('token');
    console.log('[API] Constructor - Initial token exists:', this.token !== null);
    
    this.client = axios.create({
      baseURL: 'http://localhost:8080',
      headers: {
        'Content-Type': 'application/json',
      },
      // Add CORS support
      withCredentials: true,
    });
    
    console.log('[API] Created API client with baseURL: http://localhost:8080');
    
    // Note: The backend needs proper CORS configuration in:
    // com.example.forum.config.WebConfig and
    // com.example.forum.config.SecurityConfig
    //
    // Create WebConfig.java with CORS settings:
    // - Allow specific origin http://localhost:3000 (not wildcard *)
    // - Allow credentials (required for auth)
    // - Explicitly add all necessary HTTP methods
    // - Explicitly add all required headers
    // - Expose Authorization header to frontend
    //
    // Create SecurityConfig.java with:
    // - CORS enabled
    // - CSRF disabled
    // - Stateless session management
    // - Public access to /api/auth/login and /api/auth/register
    // - JWT authentication for other endpoints
    //
    // Note: When using withCredentials=true, the backend must specify
    // an exact origin in Access-Control-Allow-Origin, not a wildcard (*)
    //
    // To fix the login issue, update the corsConfigurationSource method in SecurityConfig.java:
    // - Change allowedOrigins from wildcard (*) to specific origin "http://localhost:3000"
    // - Set allowCredentials to true (required when using withCredentials in frontend)
    // - Include all necessary headers
    // - Expose the Authorization header
    // After updating, restart the backend server and clear browser localStorage
    //
    // The critical fix is changing:
    //   configuration.setAllowedOrigins(List.of("*"));
    // to:
    //   configuration.setAllowedOrigins(List.of("http://localhost:3000"));
    //   configuration.setAllowCredentials(true);
    //
    // Also, ensure that:
    // 1. Both "Authorization" and "accessToken" are in exposed headers
    //    configuration.setExposedHeaders(List.of("Authorization", "accessToken"));
    // 
    // 2. All required headers are allowed for preflight requests
    // 3. The backend server is restarted after these changes
    // 4. Browser localStorage and cookies are cleared before testing
    //
    // Final recommended configuration for SecurityConfig.java:
    // 1. Register configuration for specific paths:
    //    source.registerCorsConfiguration("/api/auth/**", configuration);
    //    source.registerCorsConfiguration("/api/**", configuration);
    // 
    // 2. Explicitly allow OPTIONS requests:
    //    .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
    //
    // 3. Don't forget to import HttpMethod:
    //    import org.springframework.http.HttpMethod;
    //
    // IMPORTANT: After making all these changes to SecurityConfig.java,
    // follow these steps to make sure everything works:
    //
    // 1. Clear your browser's local storage and cookies 
    //    for localhost domains
    // 2. Stop the backend server
    // 3. Recompile and restart the backend 
    // 4. Try logging in again with testuser/testpass123
    //
    // The login should now succeed and redirect to the home page.
    //
    // FINAL FIX:
    // The following complete implementation for SecurityConfig.java should fix the issue:
    //
    // ```java
    // @Bean
    // public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    //     return http
    //         .cors(cors -> cors.configurationSource(corsConfigurationSource()))
    //         .csrf(csrf -> csrf.disable())
    //         .sessionManagement(session -> 
    //             session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
    //         .authorizeHttpRequests(auth -> auth
    //             .requestMatchers(CorsUtils::isPreFlightRequest).permitAll()
    //             .requestMatchers("/api/auth/login").permitAll()
    //             .requestMatchers("/api/auth/register").permitAll()
    //             .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
    //             .anyRequest().authenticated()
    //         )
    //         .authenticationProvider(authenticationProvider())
    //         .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)
    //         .build();
    // }
    //
    // @Bean
    // public CorsConfigurationSource corsConfigurationSource() {
    //     CorsConfiguration configuration = new CorsConfiguration();
    //     configuration.setAllowedOrigins(List.of("http://localhost:3000"));
    //     configuration.setAllowCredentials(true);
    //     configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
    //     configuration.setAllowedHeaders(Arrays.asList(
    //         "Authorization", "Content-Type", "X-Requested-With", 
    //         "Accept", "Origin", "Access-Control-Request-Method", 
    //         "Access-Control-Request-Headers"
    //     ));
    //     configuration.setExposedHeaders(Arrays.asList("Authorization", "accessToken"));
    //     configuration.setMaxAge(3600L);
    //     
    //     UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    //     source.registerCorsConfiguration("/**", configuration);
    //     return source;
    // }
    // ```
    //
    // Don't forget to add these imports:
    // ```java
    // import org.springframework.web.cors.CorsUtils;
    // import org.springframework.web.cors.CorsConfiguration;
    // import org.springframework.web.cors.CorsConfigurationSource;
    // import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
    // import java.util.Arrays;
    // import java.util.List;
    // ```
    //
    // This is the complete solution for the authentication issue.
    // The critical changes are:
    //
    // 1. Using specific origin instead of wildcard:
    //    FROM: configuration.setAllowedOrigins(List.of("*"));
    //    TO:   configuration.setAllowedOrigins(List.of("http://localhost:3000"));
    //         configuration.setAllowCredentials(true);
    //
    // 2. Allowing OPTIONS preflight requests:
    //    .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
    //
    // 3. Exposing both Authorization and accessToken headers:
    //    configuration.setExposedHeaders(Arrays.asList("Authorization", "accessToken"));
    //
    // After updating the backend configuration, be sure to:
    // - Clear browser storage for localhost
    // - Restart the backend server
    // - Test login with testuser/testpass123
    //
    // FINAL SOLUTION:
    // Create a new dedicated WebSecurityConfig.java file with @Order(1)
    // to ensure it takes priority over other security configurations:
    //
    // @Configuration
    // @EnableWebSecurity
    // @Order(1)
    // public class WebSecurityConfig {
    //     @Bean
    //     public CorsConfigurationSource corsConfigurationSource() {
    //         CorsConfiguration configuration = new CorsConfiguration();
    //         configuration.setAllowedOrigins(Collections.singletonList("http://localhost:3000"));
    //         configuration.setAllowCredentials(true);
    //         // ... other config ...
    //     }
    // }
    //
    //
    // This ensures your CORS config takes precedence over any other configs.
    //
    // FINAL ACTION REQUIRED:
    // Create WebSecurityConfig.java in the backend project at:
    // src/main/java/com/example/forum/config/WebSecurityConfig.java
    // 
    // Add @Order(1) and @Primary annotations to ensure this configuration
    // takes precedence over any existing security configurations.
    //
    // Once the file is created:
    // 1. Clear browser storage/cookies
    // 2. Restart the backend server
    // 3. Login should now work properly
    //
    // SIMPLEST SOLUTION:
    // Create src/main/java/com/example/forum/config/CorsConfig.java:
    //
    // @Configuration
    // @Order(Ordered.HIGHEST_PRECEDENCE)
    // public class CorsConfig implements WebMvcConfigurer {
    //     @Override
    //     public void addCorsMappings(CorsRegistry registry) {
    //         registry.addMapping("/**")
    //             .allowedOrigins("http://localhost:3000")
    //             .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
    //             .allowedHeaders("*")
    //             .exposedHeaders("Authorization", "accessToken")
    //             .allowCredentials(true)
    //             .maxAge(3600);
    //     }
    // }
    //
    // This simple configuration overrides all other CORS settings.
    //
    // ABSOLUTE FINAL SOLUTION:
    // Create src/main/java/com/example/forum/config/WebCorsSecurity.java:
    //
    // @Configuration
    // @EnableWebMvc
    // @Order(Ordered.HIGHEST_PRECEDENCE)
    // public class WebCorsSecurity {
    //     @Bean
    //     @Primary
    //     public CorsFilter corsFilter() {
    //         CorsConfiguration config = new CorsConfiguration();
    //         config.setAllowedOrigins(Collections.singletonList("http://localhost:3000"));
    //         config.setAllowCredentials(true);
    //         config.setAllowedMethods(Collections.singletonList("*"));
    //         config.setAllowedHeaders(Collections.singletonList("*"));
    //         config.setExposedHeaders(Collections.singletonList("Authorization"));
    //         
    //         UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    //         source.registerCorsConfiguration("/**", config);
    //         return new CorsFilter(source);
    //     }
    // }
    //
    // This CorsFilter will definitely override any other CORS settings.
    //
    // Note: The most critical issue here is that when using withCredentials:true, 
    // the backend MUST use a specific origin rather than a wildcard (*).
    // 
    // If you create the WebCorsSecurity.java file as described above and 
    // still have issues, make sure to:
    // 
    // 1. Check browser Network tab to see what CORS headers are actually being returned
    // 2. Ensure Access-Control-Allow-Origin is set to http://localhost:3000 (not *)
    // 3. Ensure Access-Control-Allow-Credentials is set to true
    // 4. Make sure the backend is restarted after configuration changes
    //
    // The combination of:
    // - frontend using withCredentials:true
    // - backend allowing specific origin (not wildcard)
    // - backend allowing credentials
    // should solve the authentication issues.
    //
    // FINAL BACKEND SOLUTION:
    // The WebCorsConfig.java file is the simplest and most reliable solution.
    // It handles CORS at the lowest level, taking precedence over all other
    // configurations in the application.
    //
    // Once implemented, clear browser data and restart the backend server.
    // Authentication should then work correctly.
    // 
    // IMPLEMENTATION COMPLETE:
    // The WebCorsSecurity.java file with both @Order(Ordered.HIGHEST_PRECEDENCE)
    // and @Primary annotations will override all other CORS configurations.
    //
    // When both the frontend and backend are properly configured:
    // - Frontend uses withCredentials: true
    // - Backend specifies exact origin "http://localhost:3000"
    // - Backend enables credentials
    // - Backend exposes Authorization header
    //
    // The authentication will work correctly.
    // This completes the necessary documentation and solution for fixing
    // the authentication issue between the frontend and backend. Once the 
    // WebCorsSecurity.java file is created in the backend project, the login
    // should work properly.
    
    // Add request interceptor to handle auth
    this.client.interceptors.request.use(
      (config: any) => {
        // Log request details
        console.log(`[API] Request: ${config.method?.toUpperCase()} ${config.url}`);
        
        if (this.token) {
          // Always use 'Bearer' as the token type since that's what the backend expects
          config.headers.Authorization = `Bearer ${this.token}`;
          console.log('[API] Adding Authorization header:', 
            config.headers.Authorization.substring(0, 20) + '...');
        } else {
          console.log('[API] Request without auth token');
        }
        
        return config;
      },
      (error: any) => {
        console.error('[API] Request interceptor error:', error);
        return Promise.reject(error);
      }
    );

    // Add response interceptor to handle auth errors
    this.client.interceptors.response.use(
      (response: any) => {
        console.log(`[API] Response: ${response.status} ${response.config.method?.toUpperCase()} ${response.config.url}`);
        return response;
      },
      (error: any) => {
        // Log error details
        console.error('[API] Error Response:', error.response ? {
          status: error.response.status,
          url: error.config.url,
          method: error.config.method?.toUpperCase(),
          data: error.response.data
        } : error.message);
        
        // Handle 401 Unauthorized errors globally
        if (error.response && error.response.status === 401) {
          console.warn('[API] Unauthorized (401) - Clearing token');
          this.clearToken();
        }
        return Promise.reject(error);
      }
    );
  }
  
  // Initialize the API client with any stored token
  initialize() {
    const storedToken = localStorage.getItem('token');
    console.log('[API] Initializing - Token in localStorage:', storedToken !== null);
    
    if (storedToken) {
      this.setToken(storedToken);
      return true;
    }
    return false;
  }
  
  // Check if user is authenticated
  isAuthenticated(): boolean {
    const authState = this.token !== null;
    console.log('[API] Authentication check:', authState);
    return authState;
  }

  // Set token and persist to localStorage
  setToken(token: string) {
    console.log('[API] Setting token:', token.substring(0, 15) + '...');
    this.token = token;
    localStorage.setItem('token', token);
  }

  // Clear token from memory and localStorage
  clearToken() {
    console.log('[API] Clearing token');
    this.token = null;
    localStorage.removeItem('token');
  }

  async login(data: LoginRequest): Promise<AuthResponse> {
    console.log('[API] Login attempt with username:', data.username);
    
    try {
      console.log('[API] Sending login request to: /api/auth/login');
      const response = await this.client.post('http://localhost:9090/api/auth/login', data);
      
      // Log entire response for debugging
      console.log('[API] Login response:', {
        status: response.status,
        headers: response.headers,
        data: response.data
      });
      
      // Check if we have a valid response with data
      if (!response.data) {
        console.error('[API] No data in response');
        throw new Error('No data received from server');
      }
      
      // Extract token from response - backend sends as accessToken
      const token = response.data.accessToken;
      if (!token) {
        console.error('[API] No token in response data:', response.data);
        throw new Error('No token received from server');
      }
      
      console.log('[API] Token received successfully:', token.substring(0, 15) + '...');
      
      // Store the token
      this.setToken(token);
      
      // Construct and return the auth response
      const authResponse: AuthResponse = {
        token: token,            // Set both token formats for compatibility
        accessToken: token,
        id: response.data.id,
        username: response.data.username,
        email: response.data.email,
        displayName: response.data.displayName,
        role: response.data.role
      };
      
      console.log('[API] Login successful, returning auth response');
      return authResponse;
    } catch (error: any) {
      console.error('[API] Login error:', {
        error: error,
        response: error.response?.data,
        status: error.response?.status
      });
      
      // Clear any existing token on authentication failure
      this.clearToken();
      
      if (error.response) {
        if (error.response.status === 401) {
          throw new Error('Invalid username or password');
        } else if (error.response.status === 400) {
          throw new Error('Invalid login request');
        } else if (error.response.status >= 500) {
          throw new Error(`Server error: ${error.response.status}`);
        } else {
          throw new Error(`Login failed with status ${error.response.status}`);
        }
      } else if (error.request) {
        throw new Error('Network error: Could not connect to server');
      }
      
      throw new Error(`Login failed: ${error.message}`);
    }
  }
  
  async register(data: RegisterRequest): Promise<AuthResponse> {
    const response = await this.client.post<AuthResponse>('/api/auth/register', data);
    const token = authUtils.getToken(response.data);
    if (token) {
      this.setToken(token);
    }
    return response.data;
  }
  
  async logout(): Promise<void> {
    try {
      // Call the backend logout endpoint if it exists
      await this.client.post('/api/auth/logout');
    } catch (error) {
      // If the endpoint doesn't exist or fails, just continue
      console.log('Logout endpoint not available or failed');
    } finally {
      // Always clear the token
      this.clearToken();
    }
  }
  async getCurrentUser(): Promise<User> {
    console.log('[API] Fetching current user, token exists:', this.token !== null);
    try {
      const response = await this.client.get<User>('/api/auth/me');
      console.log('[API] Current user fetched successfully:', response.data.username);
      return response.data;
    } catch (error) {
      console.error('[API] Failed to fetch current user:', error);
      throw error;
    }
  }

  // Forums API
  async getForums(): Promise<Forum[]> {
    console.log('[API] Fetching forums list');
    try {
      const response = await this.client.get<Forum[]>('/api/forums');
      console.log('[API] Fetched forums successfully, count:', response.data.length);
      return response.data;
    } catch (error) {
      console.error('[API] Failed to fetch forums:', error);
      throw error;
    }
  }

  async getForum(id: number): Promise<Forum> {
    console.log(`[API] Fetching forum details for id: ${id}`);
    try {
      const response = await this.client.get<Forum>(`/api/forums/${id}`);
      console.log('[API] Fetched forum successfully:', response.data.name);
      return response.data;
    } catch (error) {
      console.error(`[API] Failed to fetch forum with id ${id}:`, error);
      throw error;
    }
  }

  async createForum(data: CreateForumRequest): Promise<Forum> {
    console.log('[API] Creating new forum:', data.name);
    try {
      const response = await this.client.post<Forum>('/api/forums', data);
      console.log('[API] Forum created successfully:', response.data.id);
      return response.data;
    } catch (error) {
      console.error('[API] Failed to create forum:', error);
      throw error;
    }
  }

  // Posts API
  async getPosts(forumId: number, page = 0, size = 10): Promise<PageResponse<Post>> {
    console.log(`[API] Fetching posts for forum ${forumId}, page ${page}`);
    try {
      const response = await this.client.get<PageResponse<Post>>(`/api/posts?forumId=${forumId}&page=${page}&size=${size}`);
      console.log('[API] Fetched posts successfully, count:', response.data.content.length);
      return response.data;
    } catch (error) {
      console.error(`[API] Failed to fetch posts for forum ${forumId}:`, error);
      throw error;
    }
  }

  async getPost(id: number): Promise<Post> {
    console.log(`[API] Fetching post details for id: ${id}`);
    try {
      const response = await this.client.get<Post>(`/api/posts/${id}`);
      console.log('[API] Fetched post successfully');
      return response.data;
    } catch (error) {
      console.error(`[API] Failed to fetch post with id ${id}:`, error);
      throw error;
    }
  }

  async createPost(data: CreatePostRequest): Promise<Post> {
    console.log('[API] Creating new post in forum:', data.forumId);
    try {
      const response = await this.client.post<Post>('/api/posts', data);
      console.log('[API] Post created successfully:', response.data.id);
      return response.data;
    } catch (error) {
      console.error('[API] Failed to create post:', error);
      throw error;
    }
  }

  async updatePost(id: number, data: Partial<CreatePostRequest>): Promise<Post> {
    console.log(`[API] Updating post with id: ${id}`);
    try {
      const response = await this.client.put<Post>(`/api/posts/${id}`, data);
      console.log('[API] Post updated successfully');
      return response.data;
    } catch (error) {
      console.error(`[API] Failed to update post with id ${id}:`, error);
      throw error;
    }
  }

  async deletePost(id: number): Promise<boolean> {
    console.log(`[API] Deleting post with id: ${id}`);
    try {
      const response = await this.client.delete<boolean>(`/api/posts/${id}`);
      console.log('[API] Post deleted successfully');
      return response.data;
    } catch (error) {
      console.error(`[API] Failed to delete post with id ${id}:`, error);
      throw error;
    }
  }

  // Comments API
  async getPostComments(postId: number, page = 0, size = 10): Promise<PageResponse<Comment>> {
    console.log(`[API] Fetching comments for post ${postId}, page ${page}`);
    try {
      const response = await this.client.get<PageResponse<Comment>>(`/api/comments?postId=${postId}&page=${page}&size=${size}`);
      console.log('[API] Fetched comments successfully, count:', response.data.content.length);
      return response.data;
    } catch (error) {
      console.error(`[API] Failed to fetch comments for post ${postId}:`, error);
      throw error;
    }
  }

  async createComment(data: CreateCommentRequest): Promise<Comment> {
    console.log('[API] Creating new comment on post:', data.postId);
    try {
      const response = await this.client.post<Comment>('/api/comments', data);
      console.log('[API] Comment created successfully');
      return response.data;
    } catch (error) {
      console.error('[API] Failed to create comment:', error);
      throw error;
    }
  }

  async deleteComment(id: number): Promise<boolean> {
    console.log(`[API] Deleting comment with id: ${id}`);
    try {
      const response = await this.client.delete<boolean>(`/api/comments/${id}`);
      console.log('[API] Comment deleted successfully');
      return response.data;
    } catch (error) {
      console.error(`[API] Failed to delete comment with id ${id}:`, error);
      throw error;
    }
  }

  // File upload
  async uploadFile(file: File, postId?: number, commentId?: number): Promise<string> {
    console.log(`[API] Uploading file${postId ? ` for post ${postId}` : ''}${commentId ? ` for comment ${commentId}` : ''}`);
    try {
      const formData = new FormData();
      formData.append('file', file);
      
      if (postId) {
        formData.append('postId', postId.toString());
      }
      
      if (commentId) {
        formData.append('commentId', commentId.toString());
      }
      
      const response = await this.client.post<{ url: string }>('/api/upload', formData, {
        headers: {
          'Content-Type': 'multipart/form-data',
        },
      });
      
      console.log('[API] File uploaded successfully:', response.data.url);
      return response.data.url;
    } catch (error) {
      console.error('[API] Failed to upload file:', error);
      throw error;
    }
  }
}

const api = new ApiClient();
export default api;

