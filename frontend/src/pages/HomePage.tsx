import React, { useState, useEffect } from 'react';
import { 
  Typography, 
  Grid, 
  Card, 
  CardContent, 
  CardActionArea,
  CardActions,
  Button,
  Box,
  Divider,
  Chip,
  Paper,
  Container,
  Skeleton,
  Alert,
  Stack
} from '@mui/material';
import { 
  ArrowForward as ArrowForwardIcon, 
  Login as LoginIcon, 
  PersonAdd as PersonAddIcon,
  Forum as ForumIcon,
  Message as MessageIcon,
  Group as GroupIcon
} from '@mui/icons-material';
import { Link as RouterLink } from 'react-router-dom';
import api from '../services/api';
import { Forum, Post } from '../types';

const HomePage: React.FC = () => {
  const [recentForums, setRecentForums] = useState<Forum[]>([]);
  const [recentPosts, setRecentPosts] = useState<Post[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);
  const [isAuthenticated, setIsAuthenticated] = useState<boolean>(
    localStorage.getItem('token') !== null
  );

  useEffect(() => {
    const fetchHomeData = async () => {
      try {
        setLoading(true);
        
        // Check if user is authenticated
        if (!isAuthenticated) {
          // For unauthenticated users, we'll set a timeout to simulate loading
          // and then show the welcome content
          setTimeout(() => {
            setLoading(false);
          }, 500);
          return;
        }
        
        // Fetch forums for the sidebar
        const forums = await api.getForums();
        setRecentForums(forums.slice(0, 3)); // Just show the 3 most recent

        // Get the latest posts from the first forum if available
        if (forums.length > 0) {
          const posts = await api.getPosts(forums[0].id, 0, 5);
          setRecentPosts(posts.content);
        }
        
        setError(null);
      } catch (err: any) {
        console.error('Error fetching home data:', err);
        
        // Handle different types of errors
        if (err.response) {
          // The request was made and the server responded with a status code
          // that falls out of the range of 2xx
          if (err.response.status === 401) {
            setIsAuthenticated(false);
            localStorage.removeItem('token');
            setError('Your session has expired. Please log in again.');
          } else {
            setError(`Server error: ${err.response.data?.message || 'Failed to load content. Please try again later.'}`);
          }
        } else if (err.request) {
          // The request was made but no response was received
          setError('Network error: Unable to connect to the server. Please check your internet connection.');
        } else {
          // Something happened in setting up the request that triggered an Error
          setError('An unexpected error occurred. Please try again later.');
        }
      } finally {
        setLoading(false);
      }
    };

    fetchHomeData();
  }, [isAuthenticated]);

  // Render authenticated content with recent posts and forums
  const renderAuthenticatedContent = () => (
    <Grid container spacing={4}>
      {/* Main content area */}
      <Grid item xs={12} md={8}>
        <Paper variant="outlined" sx={{ p: 3, mb: 3 }}>
          <Typography variant="h5" component="h2" gutterBottom>
            Recent Discussions
          </Typography>
          <Divider sx={{ mb: 2 }} />

          {loading ? (
            // Skeleton for posts
            <Stack spacing={2}>
              {Array.from(new Array(3)).map((_, index) => (
                <Box key={index}>
                  <Skeleton animation="wave" height={24} width="60%" />
                  <Skeleton animation="wave" height={18} width="40%" sx={{ mb: 1 }} />
                  <Skeleton animation="wave" height={60} />
                </Box>
              ))}
            </Stack>
          ) : recentPosts.length === 0 ? (
            <Typography color="text.secondary" align="center" sx={{ py: 4 }}>
              No recent discussions available.
            </Typography>
          ) : (
            <Stack spacing={2}>
              {recentPosts.map((post) => (
                <Box key={post.id} sx={{ mb: 2 }}>
                  <Typography variant="h6" component="h3">
                    <RouterLink to={`/posts/${post.id}`} style={{ textDecoration: 'none', color: 'inherit' }}>
                      {post.title}
                    </RouterLink>
                  </Typography>
                  <Box sx={{ display: 'flex', mb: 1 }}>
                    <Chip 
                      size="small" 
                      label={post.author.username} 
                      sx={{ mr: 1 }} 
                    />
                    <Chip 
                      size="small" 
                      label={new Date(post.createdAt).toLocaleDateString()} 
                      variant="outlined" 
                    />
                  </Box>
                  <Typography variant="body2" sx={{ mb: 1 }}>
                    {post.content.length > 150 
                      ? `${post.content.substring(0, 150)}...` 
                      : post.content}
                  </Typography>
                  <Button 
                    size="small" 
                    endIcon={<ArrowForwardIcon />}
                    component={RouterLink}
                    to={`/posts/${post.id}`}
                  >
                    Read More
                  </Button>
                </Box>
              ))}
            </Stack>
          )}

          <Box sx={{ mt: 3, textAlign: 'right' }}>
            <Button 
              variant="outlined" 
              component={RouterLink} 
              to="/forums"
              endIcon={<ArrowForwardIcon />}
            >
              Explore All Forums
            </Button>
          </Box>
        </Paper>
      </Grid>

      {/* Sidebar */}
      <Grid item xs={12} md={4}>
        <Paper variant="outlined" sx={{ p: 3, mb: 3 }}>
          <Typography variant="h6" gutterBottom>
            Recent Forums
          </Typography>
          <Divider sx={{ mb: 2 }} />
          
          {loading ? (
            // Skeleton for forums
            <Stack spacing={2}>
              {Array.from(new Array(3)).map((_, index) => (
                <Box key={index}>
                  <Skeleton animation="wave" height={24} width="80%" />
                  <Skeleton animation="wave" height={30} />
                </Box>
              ))}
            </Stack>
          ) : recentForums.length === 0 ? (
            <Typography color="text.secondary" align="center" sx={{ py: 2 }}>
              No forums available.
            </Typography>
          ) : (
            <Stack spacing={2}>
              {recentForums.map((forum) => (
                <Card key={forum.id} variant="outlined">
                  <CardActionArea component={RouterLink} to={`/forums/${forum.id}`}>
                    <CardContent>
                      <Typography variant="subtitle1" gutterBottom>
                        {forum.name}
                      </Typography>
                      <Typography variant="body2" color="text.secondary" noWrap>
                        {forum.description}
                      </Typography>
                    </CardContent>
                  </CardActionArea>
                  <CardActions>
                    <Button 
                      size="small" 
                      endIcon={<ArrowForwardIcon />}
                      component={RouterLink}
                      to={`/forums/${forum.id}`}
                    >
                      View
                    </Button>
                  </CardActions>
                </Card>
              ))}
            </Stack>
          )}

          <Box sx={{ mt: 3, textAlign: 'right' }}>
            <Button 
              variant="outlined" 
              component={RouterLink} 
              to="/forums"
            >
              All Forums
            </Button>
          </Box>
        </Paper>
      </Grid>
    </Grid>
  );

  // Render content for unauthenticated users
  const renderUnauthenticatedContent = () => (
    <Grid container spacing={4} justifyContent="center">
      <Grid item xs={12} md={8}>
        <Paper sx={{ p: 4, textAlign: 'center', borderRadius: 2, mb: 3 }}>
          <Typography variant="h4" gutterBottom color="primary">
            Welcome to our Forum Community
          </Typography>
          <Typography variant="body1" paragraph>
            Join our community to participate in discussions, share your thoughts, and connect with others.
          </Typography>
          <Box sx={{ mt: 4, display: 'flex', justifyContent: 'center', gap: 2 }}>
            <Button 
              variant="contained" 
              size="large" 
              component={RouterLink} 
              to="/register"
              startIcon={<PersonAddIcon />}
            >
              Sign Up
            </Button>
            <Button 
              variant="outlined" 
              size="large" 
              component={RouterLink} 
              to="/login"
              startIcon={<LoginIcon />}
            >
              Sign In
            </Button>
          </Box>
        </Paper>

        <Paper sx={{ p: 4, borderRadius: 2 }}>
          <Typography variant="h5" gutterBottom>
            Why Join Our Forum?
          </Typography>
          <Divider sx={{ mb: 3 }} />
          <Grid container spacing={3}>
            <Grid item xs={12} md={4}>
              <Box sx={{ textAlign: 'center', p: 2 }}>
                <ForumIcon color="primary" sx={{ fontSize: 48, mb: 1 }} />
                <Typography variant="h6" gutterBottom>
                  Join Discussions
                </Typography>
                <Typography variant="body2" color="text.secondary">
                  Participate in community discussions on topics that interest you.
                </Typography>
              </Box>
            </Grid>
            <Grid item xs={12} md={4}>
              <Box sx={{ textAlign: 'center', p: 2 }}>
                <MessageIcon color="primary" sx={{ fontSize: 48, mb: 1 }} />
                <Typography variant="h6" gutterBottom>
                  Share Knowledge
                </Typography>
                <Typography variant="body2" color="text.secondary">
                  Share your expertise and learn from others in our community.
                </Typography>
              </Box>
            </Grid>
            <Grid item xs={12} md={4}>
              <Box sx={{ textAlign: 'center', p: 2 }}>
                <GroupIcon color="primary" sx={{ fontSize: 48, mb: 1 }} />
                <Typography variant="h6" gutterBottom>
                  Connect with Others
                </Typography>
                <Typography variant="body2" color="text.secondary">
                  Build connections with like-minded individuals in our community.
                </Typography>
              </Box>
            </Grid>
          </Grid>
        </Paper>
      </Grid>
    </Grid>
  );

  return (
    <Container maxWidth="lg">
      <Box sx={{ mb: 4 }}>
        <Typography variant="h4" component="h1" gutterBottom>
          {isAuthenticated ? 'Welcome to Forums' : 'Join Our Community Forum'}
        </Typography>
        <Typography variant="subtitle1" color="text.secondary" gutterBottom>
          {isAuthenticated 
            ? 'Join the conversation and connect with other members' 
            : 'Create an account to participate in discussions and connect with others'}
        </Typography>
      </Box>

      {error && (
        <Alert severity="error" sx={{ mb: 3 }}>
          {error}
        </Alert>
      )}

      {loading && !isAuthenticated ? (
        <Box sx={{ display: 'flex', justifyContent: 'center', my: 4 }}>
          <Skeleton variant="rectangular" width="100%" height={400} />
        </Box>
      ) : isAuthenticated ? (
        renderAuthenticatedContent()
      ) : (
        renderUnauthenticatedContent()
      )}
    </Container>
  );
};

export default HomePage;
