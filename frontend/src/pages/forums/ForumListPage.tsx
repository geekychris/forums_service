import React, { useState, useEffect } from 'react';
import { 
  Typography, 
  Grid, 
  Card, 
  CardContent, 
  CardActionArea,
  CardActions,
  Button,
  Skeleton,
  Box,
  Fab,
  Divider,
  Chip,
  Alert
} from '@mui/material';
import { Add as AddIcon, ArrowForward as ArrowForwardIcon } from '@mui/icons-material';
import { Link as RouterLink, useNavigate } from 'react-router-dom';
import api from '../../services/api';
import { Forum } from '../../types';
import { useAuth } from '../../App';

const ForumListPage: React.FC = () => {
  const [forums, setForums] = useState<Forum[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);
  const [sessionExpired, setSessionExpired] = useState<boolean>(false);
  const navigate = useNavigate();
  const { isAuthenticated, loading: authLoading } = useAuth();

  useEffect(() => {
    // Only fetch forums if auth loading is complete
    if (!authLoading) {
      const fetchForums = async () => {
        try {
          setLoading(true);
          setSessionExpired(false);
          const fetchedForums = await api.getForums();
          setForums(fetchedForums);
          setError(null);
        } catch (err: any) {
          console.error('Error fetching forums:', err);
          
          // Check if this is an auth error (401)
          if (err.response && err.response.status === 401) {
            setSessionExpired(true);
            setError('Your session has expired. Please log in again.');
          } else {
            setError('Failed to load forums. Please try again later.');
          }
        } finally {
          setLoading(false);
        }
      };

      fetchForums();
    }
  }, [authLoading, isAuthenticated]);

  const handleCreateForum = () => {
    navigate('/forums/create');
  };

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
    });
  };

  return (
    <div>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 4 }}>
        <Typography variant="h4" component="h1" gutterBottom>
          Forums
        </Typography>
        {isAuthenticated && (
          <Button
            variant="contained"
            color="primary"
            startIcon={<AddIcon />}
            onClick={handleCreateForum}
          >
            Create Forum
          </Button>
        )}
      </Box>
      
      {sessionExpired && (
        <Alert severity="warning" sx={{ mb: 2 }}>
          Your session has expired. Please <RouterLink to="/login">log in</RouterLink> again.
        </Alert>
      )}

      {error && !sessionExpired && (
        <Typography color="error" sx={{ mb: 2 }}>
          {error}
        </Typography>
      )}

      <Grid container spacing={3}>
        {loading ? (
          // Skeleton loading state
          Array.from(new Array(6)).map((_, index) => (
            <Grid item xs={12} sm={6} md={4} key={index}>
              <Card sx={{ height: '100%' }}>
                <CardContent>
                  <Skeleton animation="wave" height={32} width="80%" />
                  <Skeleton animation="wave" height={20} width="40%" sx={{ mt: 1 }} />
                  <Skeleton animation="wave" height={60} sx={{ mt: 2 }} />
                </CardContent>
                <CardActions>
                  <Skeleton animation="wave" height={36} width={80} />
                </CardActions>
              </Card>
            </Grid>
          ))
        ) : forums.length === 0 ? (
          <Grid item xs={12}>
            <Typography variant="h6" align="center" color="textSecondary" sx={{ mt: 4, mb: 4 }}>
              No forums available. Be the first to create one!
            </Typography>
          </Grid>
        ) : (
          forums.map((forum) => (
            <Grid item xs={12} sm={6} md={4} key={forum.id}>
              <Card sx={{ height: '100%', display: 'flex', flexDirection: 'column' }}>
                <CardActionArea 
                  component={RouterLink} 
                  to={`/forums/${forum.id}`}
                  sx={{ flexGrow: 1 }}
                >
                  <CardContent>
                    <Typography variant="h6" component="div" gutterBottom noWrap>
                      {forum.name}
                    </Typography>
                    <Chip 
                      label={`Created ${formatDate(forum.createdAt)}`} 
                      size="small" 
                      sx={{ mb: 2 }} 
                    />
                    <Typography variant="body2" color="text.secondary">
                      {forum.description || 'No description available.'}
                    </Typography>
                  </CardContent>
                </CardActionArea>
                <Divider />
                <CardActions>
                  <Button 
                    size="small" 
                    endIcon={<ArrowForwardIcon />}
                    component={RouterLink}
                    to={`/forums/${forum.id}`}
                  >
                    View Forum
                  </Button>
                </CardActions>
              </Card>
            </Grid>
          ))
        )}
      </Grid>

      {isAuthenticated && (
        <Fab
          color="primary"
          aria-label="add"
          sx={{ position: 'fixed', bottom: 16, right: 16 }}
          onClick={handleCreateForum}
        >
          <AddIcon />
        </Fab>
      )}
    </div>
  );
};

export default ForumListPage;

