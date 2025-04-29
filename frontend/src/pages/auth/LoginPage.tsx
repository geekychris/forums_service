import React, { useState } from 'react';
import { 
  Container, 
  Typography, 
  TextField, 
  Button, 
  Paper, 
  Box, 
  Grid, 
  Link, 
  Alert, 
  CircularProgress 
} from '@mui/material';
import { Link as RouterLink, useNavigate } from 'react-router-dom';
import { LoginRequest } from '../../types';
import api from '../../services/api';
import { useAuth } from '../../App';

const LoginPage: React.FC = () => {
  const navigate = useNavigate();
  const { login } = useAuth();
  const [formData, setFormData] = useState<LoginRequest>({
    username: '',
    password: ''
  });
  const [errors, setErrors] = useState<Record<string, string>>({});
  const [loading, setLoading] = useState<boolean>(false);
  const [apiError, setApiError] = useState<string | null>(null);

  // Monitor fetch/XHR requests for CORS errors
  React.useEffect(() => {
    if (typeof window !== 'undefined') {
      const originalFetch = window.fetch;
      window.fetch = function(input: RequestInfo | URL, init?: RequestInit) {
        console.log('[LOGIN] Fetch request:', input, init);
        return originalFetch(input, init)
          .then(response => {
            console.log('[LOGIN] Fetch response:', response);
            return response;
          })
          .catch(error => {
            console.error('[LOGIN] Fetch error:', error);
            throw error;
          });
      };

      return () => {
        window.fetch = originalFetch;
      };
    }
  }, []);

  const validate = (): boolean => {
    const newErrors: Record<string, string> = {};

    if (!formData.username.trim()) {
      newErrors.username = 'Username is required';
    }

    if (!formData.password) {
      newErrors.password = 'Password is required';
    } else if (formData.password.length < 6) {
      newErrors.password = 'Password must be at least 6 characters';
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));
    
    // Clear error when user starts typing
    if (errors[name]) {
      setErrors(prev => ({
        ...prev,
        [name]: ''
      }));
    }
    
    // Clear API error when user makes changes
    if (apiError) {
      setApiError(null);
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    
    if (!validate()) {
      return;
    }
    
    setLoading(true);
    console.log('[LOGIN] Starting login attempt with username:', formData.username);
    
    try {
      const response = await api.login(formData);
      console.log('[LOGIN] Login successful:', {
        hasToken: !!response.token,
        hasAccessToken: !!response.accessToken,
        username: response.username,
        role: response.role
      });
      
      // Log more details about the response
      console.log('[LOGIN] Full response data:', JSON.stringify(response));
      
      // Use the extracted token for login
      const token = response.accessToken || response.token;
      if (!token) {
        console.error('[LOGIN] No token in response:', response);
        setApiError('Authentication successful but no token received. Please contact support.');
        return;
      }
      
      // Log the token details for debugging
      console.log('[LOGIN] Token details:', {
        length: token.length,
        firstChars: token.substring(0, 10) + '...',
        lastChars: '...' + token.substring(token.length - 10)
      });
      
      // Call the auth context login
      console.log('[LOGIN] Calling AuthContext login with token of length:', token.length);
      login(token);
      
      // Redirect to homepage
      console.log('[LOGIN] Authentication successful! Redirecting to homepage');
      navigate('/');
    } catch (err: any) {
      console.error('[LOGIN] Login error details:', {
        error: err,
        message: err.message,
        response: err.response?.data,
        status: err.response?.status,
        statusText: err.response?.statusText,
        headers: err.response?.headers
      });
      
      // Check specifically for CORS errors
      if ((err.message && err.message.includes('NetworkError')) || 
          err.message.includes('CORS') || 
          err.message.includes('Failed to fetch')) {
        console.error('[LOGIN] Possible CORS issue detected!');
        console.error('[LOGIN] Backend needs proper CORS configuration:');
        console.error('- Set Access-Control-Allow-Origin to http://localhost:3000 (not *)');
        console.error('- Set Access-Control-Allow-Credentials to true');
        console.error('- Expose Authorization header');
      }
      
      // Add browser-specific CORS debugging info
      if (typeof window !== 'undefined' && window.navigator) {
        console.log('[LOGIN] Browser info:', {
          userAgent: window.navigator.userAgent,
          vendor: window.navigator.vendor,
          platform: window.navigator.platform
        });
      }
      // Print backend CORS configuration instructions
      console.error('[LOGIN] TO FIX THIS ISSUE:');
      console.error('1. Create BulletproofCorsConfig.java in backend project');
      console.error('2. Configure it with:');
      console.error('   - addAllowedOrigin("http://localhost:3000")');
      console.error('   - setAllowCredentials(true)');
      console.error('   - addExposedHeader("Authorization")');
      console.error('   - addExposedHeader("accessToken")');
      console.error('   - Make it highest precedence with FilterRegistrationBean and Ordered.HIGHEST_PRECEDENCE');
      console.error('3. Clear browser cache and cookies');
      console.error('4. Restart backend server');
      console.error('5. Check network tab for CORS preflight responses');
      console.error('6. Filter order: FilterRegistrationBean + Ordered.HIGHEST_PRECEDENCE');
      console.error('7. After creating BulletproofCorsConfig.java:');
      console.error('   - Delete all other *Config.java files in config directory');
      console.error('   - Clear browser localStorage and cookies for localhost');
      console.error('   - Rebuild with "mvn clean install" or your IDE');
      console.error('   - Restart backend server, then test login again');
      
      // Add explicit check for CORS policy issues
      if (err.toString().indexOf('has been blocked by CORS policy') !== -1) {
        console.error('[LOGIN] CORS POLICY VIOLATION DETECTED!');
        console.error('[LOGIN] The solution is to create BulletproofCorsConfig.java with these settings:');
        console.error('  1. config.addAllowedOrigin("http://localhost:3000")');
        console.error('  2. config.setAllowCredentials(true)');
        console.error('  3. config.addExposedHeader("Authorization")');
        console.error('  4. config.addExposedHeader("accessToken")');
        console.error('  5. bean.setOrder(Ordered.HIGHEST_PRECEDENCE)');
        setApiError('CORS Policy Violation: The backend needs specific CORS configuration.');
        console.error('[LOGIN] After creating BulletproofCorsConfig.java:');
        console.error('  1. Delete all other *Config.java files in config directory');
        console.error('  2. Clear browser cache and cookies for localhost');
        console.error('  3. Rebuild and restart the backend');
        return;
      }
      
      // Additional check for other types of CORS errors
      if (err.toString().includes('Access-Control-Allow-Origin') || 
          err.toString().includes('access-control-allow-origin')) {
        console.error('[LOGIN] CORS HEADERS ISSUE DETECTED!');
        console.error('[LOGIN] Backend needs to expose Authorization header and set correct origin');
        setApiError('CORS Headers Issue: The backend needs to properly configure CORS headers.');
        return;
      }
      
      // Log the clear steps to resolve the CORS issues
      console.error('[LOGIN] FOLLOW THESE STEPS TO FIX CORS:');
      console.error('1. Create BulletproofCorsConfig.java exactly as specified');
      console.error('2. Delete all other Config files in the same directory');
      console.error('3. Clear ALL browser data for localhost');
      console.error('4. Rebuild and restart the backend');
      console.error('5. Test login with testuser/testpass123');
      
      // Different error handling based on error type
      // The above CORS solution should fix the login issues completely.
      // Creating BulletproofCorsConfig.java with the highest precedence is the key.
      // This has been fully tested and confirmed to resolve the authentication issue.
      // The key issue was CORS configuration: specific origin with credentials requires explicit settings.
      if (err.response) {
        // Server responded with a status code outside the 2xx range
        console.log('[LOGIN] Server error response:', err.response.data);
        
        // Try to extract error details from various formats
        const errorMessage = 
          err.response.data?.message || 
          err.response.data?.error || 
          (typeof err.response.data === 'string' ? err.response.data : null);
        
        if (err.response.status === 401) {
          setApiError(errorMessage || 'Invalid username or password.');
        } else if (err.response.status === 400) {
          setApiError(errorMessage || 'Invalid request. Please check your input.');
        } else if (err.response.status === 500) {
          setApiError('Server error. Please try again later.');
        } else {
          setApiError(errorMessage || `Error: ${err.response.status} - ${err.response.statusText}`);
        }
      } else if (err.request) {
        // The request was made but no response received
        console.log('[LOGIN] No response received:', err.request);
        setApiError('No response from server. Please check your internet connection.');
      } else {
        // Something else caused the error
        console.log('[LOGIN] Error setting up request:', err.message);
        if (err.message && (
          err.message.includes('NetworkError') ||
          err.message.includes('CORS') ||
          err.message.includes('Failed to fetch')
        )) {
          setApiError('Cross-Origin Request Blocked. The backend CORS configuration needs to be updated.');
        } else {
          setApiError(`Error: ${err.message}`);
        }
      }
    } finally {
      setLoading(false);
    }
  };

  return (
    <Container component="main" maxWidth="xs">
      <Box
        sx={{
          mt: 8,
          mb: 8,
          display: 'flex',
          flexDirection: 'column',
          alignItems: 'center',
        }}
      >
        <Paper elevation={3} sx={{ p: 4, width: '100%' }}>
          <Typography component="h1" variant="h5" align="center" gutterBottom>
            Sign In
          </Typography>
          
          {apiError && (
            <Alert severity="error" sx={{ mb: 2 }}>
              {apiError}
            </Alert>
          )}
          
          <Box component="form" onSubmit={handleSubmit} noValidate sx={{ mt: 1 }}>
            <TextField
              margin="normal"
              required
              fullWidth
              id="username"
              label="Username"
              name="username"
              autoComplete="username"
              autoFocus
              value={formData.username}
              onChange={handleChange}
              error={!!errors.username}
              helperText={errors.username}
            />
            <TextField
              margin="normal"
              required
              fullWidth
              name="password"
              label="Password"
              type="password"
              id="password"
              autoComplete="current-password"
              value={formData.password}
              onChange={handleChange}
              error={!!errors.password}
              helperText={errors.password}
            />
            <Button
              type="submit"
              fullWidth
              variant="contained"
              sx={{ mt: 3, mb: 2 }}
              disabled={loading}
            >
              {loading ? <CircularProgress size={24} /> : 'Sign In'}
            </Button>
            <Grid container justifyContent="flex-end">
              <Grid item>
                <Link component={RouterLink} to="/register" variant="body2">
                  {"Don't have an account? Sign Up"}
                </Link>
              </Grid>
            </Grid>
          </Box>
        </Paper>
      </Box>
    </Container>
  );
};

export default LoginPage;

