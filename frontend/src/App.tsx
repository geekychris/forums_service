import React, { useState, useEffect, createContext, useContext } from 'react';
import { Routes, Route } from 'react-router-dom';
import { 
  Container, 
  CircularProgress, 
  Box, 
  Typography 
} from '@mui/material';
import NavBar from './components/layout/NavBar';
import HomePage from './pages/HomePage';
import ForumListPage from './pages/forums/ForumListPage';
import ForumDetailPage from './pages/forums/ForumDetailPage';
import CreateForumPage from './pages/forums/CreateForumPage';
import PostDetailPage from './pages/posts/PostDetailPage';
import CreatePostPage from './pages/posts/CreatePostPage';
import EditPostPage from './pages/posts/EditPostPage';
import LoginPage from './pages/auth/LoginPage';
import RegisterPage from './pages/auth/RegisterPage';
import ProfilePage from './pages/users/ProfilePage';
import NotFoundPage from './pages/NotFoundPage';
import api from './services/api';
import { User } from './types';

// Create Auth Context
interface AuthContextType {
  isAuthenticated: boolean;
  currentUser: User | null;
  loading: boolean;
  login: (token: string) => void;
  logout: () => void;
  updateCurrentUser: (user: User) => void;
}

const AuthContext = createContext<AuthContextType>({
  isAuthenticated: false,
  currentUser: null,
  loading: true,
  login: () => {},
  logout: () => {},
  updateCurrentUser: () => {}
});

// Export hook for using the auth context
export const useAuth = () => useContext(AuthContext);

function App() {
  const [isInitialized, setIsInitialized] = useState(false);
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [currentUser, setCurrentUser] = useState<User | null>(null);
  const [loading, setLoading] = useState(true);
  
  // Add logging for state changes
  const setIsAuthenticatedWithLogging = (value: boolean) => {
    console.log('[AUTH] Setting isAuthenticated:', value);
    setIsAuthenticated(value);
  };
  
  const setCurrentUserWithLogging = (user: User | null) => {
    console.log('[AUTH] Setting currentUser:', user ? user.username : 'null');
    setCurrentUser(user);
  };

  // Initialize the app and check authentication
  useEffect(() => {
    console.log('[AUTH] Starting app initialization');
    const initializeApp = async () => {
      try {
        // Initialize API client
        console.log('[AUTH] Initializing API client');
        const hasToken = api.initialize();
        console.log('[AUTH] API client initialized, hasToken:', hasToken);
        setIsAuthenticatedWithLogging(hasToken);
        
        // If we have a token, try to get the current user
        if (hasToken) {
          console.log('[AUTH] Token found, fetching current user');
          try {
            const user = await api.getCurrentUser();
            console.log('[AUTH] Current user fetched successfully:', user.username);
            setCurrentUserWithLogging(user);
          } catch (error) {
            console.error('[AUTH] Failed to fetch current user:', error);
            // If we can't get the user, the token might be invalid
            console.warn('[AUTH] Clearing token due to user fetch failure');
            api.clearToken();
            setIsAuthenticatedWithLogging(false);
          }
        }
      } catch (error) {
        console.error('[AUTH] Error initializing app:', error);
      } finally {
        console.log('[AUTH] App initialization completed');
        setLoading(false);
        setIsInitialized(true);
      }
    };

    initializeApp();
  }, []);

  // Auth context functions
  const login = (token: string) => {
    console.log('[AUTH] Login called with token:', token.substring(0, 15) + '...');
    api.setToken(token);
    setIsAuthenticatedWithLogging(true);
    
    // Fetch current user after login
    console.log('[AUTH] Fetching user after login');
    api.getCurrentUser()
      .then(user => {
        console.log('[AUTH] User fetched after login:', user.username);
        setCurrentUserWithLogging(user);
      })
      .catch(error => {
        console.error('[AUTH] Failed to fetch user after login:', error);
        // Consider handling this failure case - might need to clear token
        console.warn('[AUTH] Authentication might be incomplete - user fetch failed');
      });
  };

  const logout = () => {
    console.log('[AUTH] Logout called');
    api.logout();
    setIsAuthenticatedWithLogging(false);
    setCurrentUserWithLogging(null);
  };

  const updateCurrentUser = (user: User) => {
    console.log('[AUTH] Updating current user:', user.username);
    setCurrentUserWithLogging(user);
  };

  // Auth context value
  const authContextValue: AuthContextType = {
    isAuthenticated,
    currentUser,
    loading,
    login,
    logout,
    updateCurrentUser
  };

  // Log auth context state
  console.log('[AUTH] Current auth state:', { 
    isAuthenticated, 
    hasCurrentUser: currentUser !== null, 
    loading, 
    isInitialized 
  });

  // Show loading indicator while initializing
  if (!isInitialized) {
    return (
      <Box sx={{ 
        display: 'flex', 
        flexDirection: 'column', 
        alignItems: 'center', 
        justifyContent: 'center', 
        height: '100vh' 
      }}>
        <CircularProgress size={60} />
        <Typography variant="h6" sx={{ mt: 2 }}>
          Loading application...
        </Typography>
      </Box>
    );
  }

  return (
    <AuthContext.Provider value={authContextValue}>
      <NavBar />
      <Container maxWidth="lg" sx={{ mt: 4, mb: 4 }}>
        <Routes>
          <Route path="/" element={<HomePage />} />
          <Route path="/forums" element={<ForumListPage />} />
          <Route path="/forums/create" element={<CreateForumPage />} />
          <Route path="/forums/:forumId" element={<ForumDetailPage />} />
          <Route path="/forums/:forumId/posts/create" element={<CreatePostPage />} />
          <Route path="/posts/:postId" element={<PostDetailPage />} />
          <Route path="/posts/:postId/edit" element={<EditPostPage />} />
          <Route path="/login" element={<LoginPage />} />
          <Route path="/register" element={<RegisterPage />} />
          <Route path="/profile" element={<ProfilePage />} />
          <Route path="*" element={<NotFoundPage />} />
        </Routes>
      </Container>
    </AuthContext.Provider>
  );
}

export default App;

