import React, { useState, useEffect } from 'react';
import { useParams, Link as RouterLink, useNavigate } from 'react-router-dom';
import {
  Typography,
  Box,
  Button,
  Card,
  CardContent,
  CardActions,
  Divider,
  Skeleton,
  Chip,
  Grid,
  Pagination,
  Alert,
  Paper
} from '@mui/material';
import { 
  Add as AddIcon, 
  Comment as CommentIcon,
  Schedule as ScheduleIcon
} from '@mui/icons-material';
import api from '../../services/api';
import { Forum, Post } from '../../types';
import { useAuth } from '../../App';

const ForumDetailPage: React.FC = () => {
  const { forumId } = useParams<{ forumId: string }>();
  const [forum, setForum] = useState<Forum | null>(null);
  const [posts, setPosts] = useState<Post[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);
  const [sessionExpired, setSessionExpired] = useState<boolean>(false);
  const [page, setPage] = useState<number>(1);
  const [totalPages, setTotalPages] = useState<number>(1);
  const navigate = useNavigate();
  const { isAuthenticated, loading: authLoading } = useAuth();

  useEffect(() => {
    // Only fetch if auth loading is complete
    if (!authLoading && forumId) {
      const fetchForumAndPosts = async () => {
        try {
          setLoading(true);
          setSessionExpired(false);
          // Fetch forum details
          const forumData = await api.getForum(parseInt(forumId, 10));
          setForum(forumData);
          
          // Fetch posts for this forum
          const postsData = await api.getPosts(parseInt(forumId, 10), page - 1, 10);
          setPosts(postsData.content);
          setTotalPages(postsData.totalPages);
          setError(null);
        } catch (err: any) {
          console.error('Error fetching forum details:', err);
          
          // Check if this is an auth error (401)
          if (err.response && err.response.status === 401) {
            setSessionExpired(true);
            setError('Your session has expired. Please log in again.');
          } else if (err.response && err.response.status === 404) {
            setError('Forum not found. It may have been moved or deleted.');
          } else {
            setError('Failed to load forum details. Please try again later.');
          }
        } finally {
          setLoading(false);
        }
      };

      fetchForumAndPosts();
    }
  }, [forumId, page, authLoading, isAuthenticated]);

  const handlePageChange = (_event: React.ChangeEvent<unknown>, value: number) => {
    setPage(value);
  };

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric'
    });
  };

  const handleCreatePost = () => {
    if (forumId) {
      navigate(`/forums/${forumId}/posts/create`);
    }
  };

  return (
    <div>
      {sessionExpired && (
        <Alert severity="warning" sx={{ mb: 3 }}>
          Your session has expired. Please <RouterLink to="/login">log in</RouterLink> again.
        </Alert>
      )}

      {error && !sessionExpired && (
        <Alert severity="error" sx={{ mb: 3 }}>
          {error}
        </Alert>
      )}
      
      {(loading || authLoading) && !forum ? (
        // Skeleton for forum
        <Box>
          <Skeleton animation="wave" height={60} width="40%" />
          <Skeleton animation="wave" height={30} width="40%" sx={{ mt: 1 }} />
          <Skeleton animation="wave" height={100} sx={{ mt: 2 }} />
        </Box>
      ) : forum ? (
        // Forum header
        <Paper elevation={0} sx={{ p: 3, mb: 4, borderRadius: 2, bgcolor: 'background.paper' }}>
          <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', mb: 2 }}>
            <Typography variant="h4" component="h1" gutterBottom>
              {forum.name}
            </Typography>
            {isAuthenticated && (
              <Button
                variant="contained"
                color="primary"
                startIcon={<AddIcon />}
                onClick={handleCreatePost}
              >
                New Post
              </Button>
            )}
          </Box>
          <Typography variant="body1" color="text.secondary" paragraph>
            {forum.description || 'No description available.'}
          </Typography>
          <Chip
            icon={<ScheduleIcon fontSize="small" />}
            label={`Created on ${formatDate(forum.createdAt)}`}
            variant="outlined"
            size="small"
          />
        </Paper>
      ) : null}

      <Typography variant="h5" component="h2" gutterBottom sx={{ mt: 4, mb: 2 }}>
        Posts
      </Typography>

      {loading && posts.length === 0 ? (
        // Skeleton for posts
        Array.from(new Array(3)).map((_, index) => (
          <Card key={index} sx={{ mb: 2 }}>
            <CardContent>
              <Skeleton animation="wave" height={28} width="80%" />
              <Skeleton animation="wave" height={20} width="40%" sx={{ mt: 1 }} />
              <Skeleton animation="wave" height={60} sx={{ mt: 2 }} />
            </CardContent>
            <CardActions>
              <Skeleton animation="wave" height={36} width={120} />
            </CardActions>
          </Card>
        ))
      ) : posts.length === 0 ? (
        <Box sx={{ textAlign: 'center', py: 4 }}>
          <Typography variant="h6" color="text.secondary" gutterBottom>
            No posts yet in this forum.
          </Typography>
          {isAuthenticated && (
            <Button
              variant="contained"
              color="primary"
              startIcon={<AddIcon />}
              onClick={handleCreatePost}
              sx={{ mt: 2 }}
            >
              Create the First Post
            </Button>
          )}
        </Box>
      ) : (
        // Posts list
        <>
          <Grid container spacing={2}>
            {posts.map((post) => (
              <Grid item xs={12} key={post.id}>
                <Card sx={{ mb: 1 }}>
                  <CardContent>
                    <Typography variant="h6" component="h3" gutterBottom>
                      {post.title}
                    </Typography>
                    <Box sx={{ display: 'flex', alignItems: 'center', mb: 1 }}>
                      <Typography variant="body2" color="text.secondary" sx={{ mr: 2 }}>
                        Posted by {post.author.username}
                      </Typography>
                      <Chip 
                        label={formatDate(post.createdAt)} 
                        size="small" 
                        variant="outlined"
                      />
                    </Box>
                    <Typography variant="body2" color="text.secondary" noWrap>
                      {post.content}
                    </Typography>
                  </CardContent>
                  <Divider />
                  <CardActions>
                    <Button
                      size="small"
                      startIcon={<CommentIcon />}
                      component={RouterLink}
                      to={`/posts/${post.id}`}
                    >
                      {post.commentCount ? `${post.commentCount} Comments` : 'View Discussion'}
                    </Button>
                  </CardActions>
                </Card>
              </Grid>
            ))}
          </Grid>
          
          {totalPages > 1 && (
            <Box sx={{ display: 'flex', justifyContent: 'center', my: 4 }}>
              <Pagination
                count={totalPages}
                page={page}
                onChange={handlePageChange}
                color="primary"
              />
            </Box>
          )}
        </>
      )}

      {isAuthenticated && posts.length > 0 && (
        <Box sx={{ position: 'fixed', bottom: 16, right: 16 }}>
          <Button
            variant="contained"
            color="primary"
            startIcon={<AddIcon />}
            onClick={handleCreatePost}
            sx={{ borderRadius: 28, px: 3, py: 1 }}
          >
            New Post
          </Button>
        </Box>
      )}
    </div>
  );
};

export default ForumDetailPage;

