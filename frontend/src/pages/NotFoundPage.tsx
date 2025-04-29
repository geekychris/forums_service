import React from 'react';
import { 
  Container, 
  Typography, 
  Button, 
  Box, 
  Paper, 
  Stack 
} from '@mui/material';
import { 
  SentimentDissatisfied as SentimentDissatisfiedIcon,
  Home as HomeIcon 
} from '@mui/icons-material';
import { useNavigate } from 'react-router-dom';

const NotFoundPage: React.FC = () => {
  const navigate = useNavigate();

  const handleGoHome = () => {
    navigate('/');
  };

  return (
    <Container maxWidth="md">
      <Box
        sx={{
          mt: 8,
          mb: 8,
          display: 'flex',
          flexDirection: 'column',
          alignItems: 'center',
        }}
      >
        <Paper 
          elevation={3} 
          sx={{ 
            p: 5, 
            width: '100%', 
            textAlign: 'center',
            borderRadius: 2
          }}
        >
          <Stack 
            spacing={3} 
            alignItems="center"
          >
            <SentimentDissatisfiedIcon 
              sx={{ 
                fontSize: 120, 
                color: 'text.secondary',
                mb: 2 
              }} 
            />
            
            <Typography variant="h2" component="h1" color="text.primary">
              404
            </Typography>
            
            <Typography variant="h5" component="h2" color="text.secondary">
              Page Not Found
            </Typography>
            
            <Typography variant="body1" color="text.secondary" paragraph>
              The page you are looking for might have been removed, had its name changed,
              or is temporarily unavailable.
            </Typography>
            
            <Button
              variant="contained"
              size="large"
              startIcon={<HomeIcon />}
              onClick={handleGoHome}
              sx={{ mt: 2 }}
            >
              Back to Home
            </Button>
          </Stack>
        </Paper>
      </Box>
    </Container>
  );
};

export default NotFoundPage;

