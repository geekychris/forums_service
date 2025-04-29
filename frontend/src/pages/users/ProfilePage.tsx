import React, { useState, useEffect, SyntheticEvent } from 'react';
import { 
  Container, 
  Typography, 
  Box, 
  Paper, 
  Avatar, 
  Grid, 
  Divider, 
  Tabs, 
  Tab, 
  Card, 
  CardContent, 
  CardActionArea, 
  Chip,
  Skeleton, 
  Alert, 
  Pagination, 
  Stack,
  Button,
  CircularProgress
} from '@mui/material';
import { 
  Person as PersonIcon,
  Email as EmailIcon,
  CalendarToday as CalendarIcon,
  Forum as ForumIcon,
  Comment as CommentIcon
} from '@mui/icons-material';
import { Link as RouterLink } from 'react-router-dom';
import api from '../../services/api';
import { User, Post, Comment, PageResponse } from '../../types';

interface TabPanelProps {
  children?: React.ReactNode;
  index: number;
  value: number;
}

function TabPanel(props: TabPanelProps) {
  const { children, value, index, ...other } = props;

  return (
    <div
      role="tabpanel"
      hidden={value !== index}
      id={`user-tabpanel-${index}`}
      aria-labelledby={`user-tab-${index}`}
      {...other}
    >
      {value === index && (
        <Box sx={{ py: 3 }}>
          {children}
        </Box>
      )}
    </div>
  );
}

function a11yProps(index: number) {
  return {
    id: `user-tab-${index}`,
    'aria-controls': `user-tabpanel-${index}`,
  };
}

const ProfilePage: React.FC = () => {
  const [user, setUser] = useState<User | null>(null);
  const [posts, setPosts] = useState<PageResponse<Post> | null>(null);
  const [comments, setComments] = useState<PageResponse<Comment> | null>(null);
  const [loading, setLoading] = useState<boolean>(true);
  const [postsLoading, setPostsLoading] = useState<boolean>(false);
  const [commentsLoading, setCommentsLoading] = useState<boolean>(false);
  const [error, setError] = useState<string | null>(null);
  const [tabValue, setTabValue] = useState(0);
  const [postsPage, setPostsPage] = useState(1);
  const [commentsPage, setCommentsPage] = useState(1);

  useEffect(() => {
    const fetchUserData = async () => {
      try {
        setLoading(true);
        const userData = await api.getCurrentUser();
        setUser(userData);
        
        // Initial load of user's posts
        await fetchUserPosts(1);
        
      } catch (err: any) {
        console.error('Error fetching user data:', err);
        setError(err.response?.data?.message || 'Failed to load user profile. Please try again later.');
      } finally {
        setLoading(false);
      }
    };

    fetchUserData();
  }, []);

  const fetchUserPosts = async (page: number) => {
    try {
      setPostsLoading(true);
      // Assuming the API has a method to get posts by user
      // This might need to be adjusted based on actual API
      const response = await api.getPosts(0, page - 1, 5); // Using forumId=0 as placeholder
      setPosts(response);
    } catch (err) {
      console.error('Error fetching user posts:', err);
    } finally {
      setPostsLoading(false);
    }
  };

  const fetchUserComments = async (page: number) => {
    try {
      setCommentsLoading(true);
      // Assuming the API has a method to get comments by user
      // This might need to be adjusted based on actual API
      const response = await api.getPostComments(0, page - 1, 5); // Using postId=0 as placeholder
      setComments(response);
    } catch (err) {
      console.error('Error fetching user comments:', err);
    } finally {
      setCommentsLoading(false);
    }
  };

  const handleTabChange = (_event: SyntheticEvent, newValue: number) => {
    setTabValue(newValue);
    
    // Fetch comments data when tab is switched to comments
    if (newValue === 1 && !comments) {
      fetchUserComments(1);
    }
  };

  const handlePostsPageChange = (_event: React.ChangeEvent<unknown>, value: number) => {
    setPostsPage(value);
    fetchUserPosts(value);
  };

  const handleCommentsPageChange = (_event: React.ChangeEvent<unknown>, value: number) => {
    setCommentsPage(value);
    fetchUserComments(value);
  };

  const formatDate = (dateString?: string) => {
    if (!dateString) return 'N/A';
    
    return new Date(dateString).toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
    });
  };

  if (loading) {
    return (
      <Container maxWidth="lg">
        <Box sx={{ mt: 4 }}>
          <Paper sx={{ p: 3 }}>
            <Grid container spacing={3}>
              <Grid item xs={12} md={4}>
                <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
                  <Skeleton variant="circular" width={80} height={80} sx={{ mr: 2 }} />
                  <Box>
                    <Skeleton variant="text" width={150} height={32} />
                    <Skeleton variant="text" width={100} height={24} />
                  </Box>
                </Box>
                <Skeleton variant="rectangular" height={100} />
              </Grid>
              <Grid item xs={12} md={8}>
                <Skeleton variant="text" width={200} height={40} />
                <Skeleton variant="rectangular" height={200} />
              </Grid>
            </Grid>
          </Paper>
        </Box>
      </Container>
    );
  }

  if (error) {
    return (
      <Container maxWidth="lg">
        <Box sx={{ mt: 4 }}>
          <Alert severity="error" sx={{ mb: 2 }}>
            {error}
          </Alert>
          <Button 
            variant="contained" 
            component={RouterLink} 
            to="/"
          >
            Return to Home
          </Button>
        </Box>
      </Container>
    );
  }

  return (
    <Container maxWidth="lg">
      <Box sx={{ mt: 4 }}>
        <Paper sx={{ p: 3, mb: 4 }}>
          <Grid container spacing={3}>
            {/* User profile section */}
            <Grid item xs={12} md={4}>
              <Box sx={{ display: 'flex', flexDirection: 'column', alignItems: 'center', mb: 3 }}>
                <Avatar 
                  sx={{ 
                    width: 100, 
                    height: 100, 
                    fontSize: 40, 
                    bgcolor: 'primary.main',
                    mb: 2
                  }}
                >
                  {user?.displayName?.charAt(0).toUpperCase() || 'U'}
                </Avatar>
                <Typography variant="h5" gutterBottom>
                  {user?.displayName || 'User'}
                </Typography>
                <Typography variant="body2" color="text.secondary" gutterBottom>
                  @{user?.username}
                </Typography>
              </Box>
              
              <Divider sx={{ my: 2 }} />
              
              <Stack spacing={2}>
                <Box sx={{ display: 'flex', alignItems: 'center' }}>
                  <PersonIcon sx={{ mr: 1, color: 'text.secondary' }} />
                  <Typography variant="body1">
                    {user?.displayName || 'N/A'}
                  </Typography>
                </Box>
                
                <Box sx={{ display: 'flex', alignItems: 'center' }}>
                  <EmailIcon sx={{ mr: 1, color: 'text.secondary' }} />
                  <Typography variant="body1">
                    {user?.email || 'N/A'}
                  </Typography>
                </Box>
                
                <Box sx={{ display: 'flex', alignItems: 'center' }}>
                  <CalendarIcon sx={{ mr: 1, color: 'text.secondary' }} />
                  <Typography variant="body1">
                    Joined April 2025
                  </Typography>
                </Box>
              </Stack>
              
              <Divider sx={{ my: 2 }} />
              
              <Typography variant="h6" gutterBottom>
                Activity
              </Typography>
              
              <Grid container spacing={2}>
                <Grid item xs={6}>
                  <Card variant="outlined">
                    <CardContent sx={{ textAlign: 'center' }}>
                      <ForumIcon color="primary" sx={{ fontSize: 40, mb: 1 }} />
                      <Typography variant="h6">
                        {posts?.totalElements || 0}
                      </Typography>
                      <Typography variant="body2" color="text.secondary">
                        Posts
                      </Typography>
                    </CardContent>
                  </Card>
                </Grid>
                <Grid item xs={6}>
                  <Card variant="outlined">
                    <CardContent sx={{ textAlign: 'center' }}>
                      <CommentIcon color="primary" sx={{ fontSize: 40, mb: 1 }} />
                      <Typography variant="h6">
                        {comments?.totalElements || 0}
                      </Typography>
                      <Typography variant="body2" color="text.secondary">
                        Comments
                      </Typography>
                    </CardContent>
                  </Card>
                </Grid>
              </Grid>
            </Grid>
            
            {/* User content section */}
            <Grid item xs={12} md={8}>
              <Box sx={{ width: '100%' }}>
                <Box sx={{ borderBottom: 1, borderColor: 'divider' }}>
                  <Tabs 
                    value={tabValue} 
                    onChange={handleTabChange} 
                    aria-label="user content tabs"
                    variant="fullWidth"
                  >
                    <Tab label="Posts" icon={<ForumIcon />} iconPosition="start" {...a11yProps(0)} />
                    <Tab label="Comments" icon={<CommentIcon />} iconPosition="start" {...a11yProps(1)} />
                  </Tabs>
                </Box>
                
                {/* Posts tab */}
                <TabPanel value={tabValue} index={0}>
                  {postsLoading ? (
                    <Box sx={{ display: 'flex', justifyContent: 'center' }}>
                      <CircularProgress />
                    </Box>
                  ) : posts?.content && posts.content.length > 0 ? (
                    <>
                      <Stack spacing={2}>
                        {posts.content.map((post) => (
                          <Card key={post.id} variant="outlined">
                            <CardActionArea component={RouterLink} to={`/posts/${post.id}`}>
                              <CardContent>
                                <Typography variant="h6" gutterBottom>
                                  {post.title}
                                </Typography>
                                <Box sx={{ display: 'flex', mb: 1, gap: 1, flexWrap: 'wrap' }}>
                                  <Chip 
                                    size="small" 
                                    label={formatDate(post.createdAt)} 
                                    variant="outlined" 
                                  />
                                  <Chip 
                                    size="small" 
                                    label={post.forum.name} 
                                    component={RouterLink} 
                                    to={`/forums/${post.forum.id}`}
                                    clickable
                                    color="primary"
                                    variant="outlined"
                                  />
                                </Box>
                                <Typography variant="body2" color="text.secondary" sx={{ mb: 1 }}>
                                  {post.content.length > 100 
                                    ? `${post.content.substring(0, 100)}...` 
                                    : post.content}
                                </Typography>
                              </CardContent>
                            </CardActionArea>
                          </Card>
                        ))}
                      </Stack>
                      
                      {posts.totalPages > 1 && (
                        <Box sx={{ display: 'flex', justifyContent: 'center', mt: 3 }}>
                          <Pagination 
                            count={posts.totalPages} 
                            page={postsPage}
                            onChange={handlePostsPageChange}
                            color="primary"
                          />
                        </Box>
                      )}
                    </>
                  ) : (
                    <Box sx={{ textAlign: 'center', py: 4 }}>
                      <Typography variant="subtitle1" color="text.secondary" gutterBottom>
                        You haven't created any posts yet.
                      </Typography>
                      <Button 
                        variant="contained" 
                        component={RouterLink} 
                        to="/forums"
                        sx={{ mt: 2 }}
                      >
                        Browse Forums
                      </Button>
                    </Box>
                  )}
                </TabPanel>
                
                {/* Comments tab */}
                <TabPanel value={tabValue} index={1}>
                  {commentsLoading ? (
                    <Box sx={{ display: 'flex', justifyContent: 'center' }}>
                      <CircularProgress />
                    </Box>
                  ) : comments?.content && comments.content.length > 0 ? (
                    <>
                      <Stack spacing={2}>
                        {comments.content.map((comment) => (
                          <Card key={comment.id} variant="outlined">
                            <CardActionArea component={RouterLink} to={`/posts/${comment.post.id}`}>
                              <CardContent>
                                <Typography variant="body1" gutterBottom>
                                  {comment.content.length > 150 
                                    ? `${comment.content.substring(0, 150)}...` 
                                    : comment.content}
                                </Typography>
                                <Box sx={{ display: 'flex', gap: 1, flexWrap: 'wrap' }}>
                                  <Chip 
                                    size="small" 
                                    label={formatDate(comment.createdAt)} 
                                    variant="outlined" 
                                  />
                                  <Chip 
                                    size="small" 
                                    label={`On: ${comment.post.title}`} 
                                    color="primary"
                                    variant="outlined"
                                  />
                                </Box>
                              </CardContent>
                            </CardActionArea>
                          </Card>
                        ))}
                      </Stack>
                      
                      {comments.totalPages > 1 && (
                        <Box sx={{ display: 'flex', justifyContent: 'center', mt: 3 }}>
                          <Pagination 
                            count={comments.totalPages} 
                            page={commentsPage}
                            onChange={handleCommentsPageChange}
                            color="primary"
                          />
                        </Box>
                      )}
                    </>
                  ) : (
                    <Box sx={{ textAlign: 'center', py: 4 }}>
                      <Typography variant="subtitle1" color="text.secondary" gutterBottom>
                        You haven't made any comments yet.
                      </Typography>
                      <Button 
                        variant="contained" 
                        component={RouterLink} 
                        to="/forums"
                        sx={{ mt: 2 }}
                      >
                        Browse Forums
                      </Button>
                    </Box>
                  )}
                </TabPanel>
              </Box>
            </Grid>
          </Grid>
        </Paper>
      </Box>
    </Container>
  );
};

export default ProfilePage;

