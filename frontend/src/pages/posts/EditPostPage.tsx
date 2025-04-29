import React, { useState, useEffect } from 'react';
import { 
  Container, 
  Typography, 
  TextField, 
  Button, 
  Paper, 
  Box, 
  Stack,
  Alert, 
  CircularProgress, 
  Divider,
  IconButton
} from '@mui/material';
import { 
  Save as SaveIcon, 
  Cancel as CancelIcon,
  ArrowBack as ArrowBackIcon 
} from '@mui/icons-material';
import { useParams, useNavigate, Link as RouterLink } from 'react-router-dom';
import api from '../../services/api';
import { Post } from '../../types';
import FileUpload from '../../components/common/FileUpload';

const EditPostPage: React.FC = () => {
  const { postId } = useParams<{ postId: string }>();
  const navigate = useNavigate();
  
  const [post, setPost] = useState<Post | null>(null);
  const [title, setTitle] = useState('');
  const [content, setContent] = useState('');
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [formErrors, setFormErrors] = useState<Record<string, string>>({});
  const [success, setSuccess] = useState(false);
  const [selectedFile, setSelectedFile] = useState<File | null>(null);
  const [uploadProgress, setUploadProgress] = useState(0);
  const [uploadError, setUploadError] = useState<string | null>(null);
  const [uploadedFileUrl, setUploadedFileUrl] = useState<string | null>(null);

  useEffect(() => {
    const fetchPost = async () => {
      if (!postId) {
        setError('Post ID is missing');
        setLoading(false);
        return;
      }

      try {
        setLoading(true);
        const fetchedPost = await api.getPost(parseInt(postId));
        setPost(fetchedPost);
        setTitle(fetchedPost.title);
        setContent(fetchedPost.content);
        setLoading(false);
      } catch (err: any) {
        console.error('Error fetching post:', err);
        setError(err.response?.data?.message || 'Failed to load post. Please try again later.');
        setLoading(false);
      }
    };

    fetchPost();
  }, [postId]);

  const validate = (): boolean => {
    const errors: Record<string, string> = {};

    if (!title.trim()) {
      errors.title = 'Title is required';
    }

    if (!content.trim()) {
      errors.content = 'Content is required';
    }

    setFormErrors(errors);
    return Object.keys(errors).length === 0;
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!validate()) {
      return;
    }

    setSaving(true);
    setError(null);
    
    try {
      // First upload file if selected
      let fileUrl = null;
      if (selectedFile) {
        try {
          setUploadError(null);
          
          // Simulate upload progress
          const uploadProgressInterval = setInterval(() => {
            setUploadProgress((prev) => {
              if (prev >= 90) {
                clearInterval(uploadProgressInterval);
                return prev;
              }
              return prev + 10;
            });
          }, 300);
          
          fileUrl = await api.uploadFile(selectedFile, post?.id);
          setUploadedFileUrl(fileUrl);
          
          // Set progress to 100% when complete
          setUploadProgress(100);
          clearInterval(uploadProgressInterval);
        } catch (err: any) {
          console.error('Error uploading file:', err);
          setUploadError('Failed to upload file. Please try again.');
          setSaving(false);
          return;
        }
      }

      // Then update the post
      if (post) {
        await api.updatePost(post.id, {
          title,
          content: fileUrl ? `${content}\n\n![Attachment](${fileUrl})` : content,
          forumId: post.forum.id
        });
        
        setSuccess(true);
        
        // Redirect back to post view after short delay
        setTimeout(() => {
          navigate(`/posts/${post.id}`);
        }, 1500);
      }
    } catch (err: any) {
      console.error('Error updating post:', err);
      setError(err.response?.data?.message || 'Failed to update post. Please try again later.');
    } finally {
      setSaving(false);
    }
  };

  const handleCancel = () => {
    if (post) {
      navigate(`/posts/${post.id}`);
    } else {
      navigate('/forums');
    }
  };

  const handleFileSelect = (file: File) => {
    setSelectedFile(file);
    setUploadProgress(0);
    setUploadError(null);
  };

  const handleRemoveFile = () => {
    setSelectedFile(null);
    setUploadProgress(0);
    setUploadedFileUrl(null);
    setUploadError(null);
  };

  if (loading) {
    return (
      <Container maxWidth="md">
        <Box sx={{ mt: 4, display: 'flex', justifyContent: 'center', alignItems: 'center', flexDirection: 'column' }}>
          <CircularProgress size={60} />
          <Typography variant="h6" sx={{ mt: 2 }}>
            Loading post...
          </Typography>
        </Box>
      </Container>
    );
  }

  if (error && !post) {
    return (
      <Container maxWidth="md">
        <Box sx={{ mt: 4 }}>
          <Alert severity="error" sx={{ mb: 2 }}>
            {error}
          </Alert>
          <Button
            variant="contained"
            startIcon={<ArrowBackIcon />}
            component={RouterLink}
            to="/forums"
          >
            Return to Forums
          </Button>
        </Box>
      </Container>
    );
  }

  return (
    <Container maxWidth="md">
      <Box sx={{ mt: 4, mb: 4 }}>
        <Paper sx={{ p: 3 }}>
          <Box sx={{ display: 'flex', alignItems: 'center', mb: 3 }}>
            <IconButton 
              onClick={handleCancel} 
              sx={{ mr: 1 }}
              aria-label="back"
            >
              <ArrowBackIcon />
            </IconButton>
            <Typography variant="h5" component="h1">
              Edit Post
            </Typography>
          </Box>

          <Divider sx={{ mb: 3 }} />

          {error && (
            <Alert severity="error" sx={{ mb: 3 }}>
              {error}
            </Alert>
          )}

          {success && (
            <Alert severity="success" sx={{ mb: 3 }}>
              Post updated successfully! Redirecting...
            </Alert>
          )}

          <Box component="form" onSubmit={handleSubmit} noValidate>
            <Stack spacing={3}>
              <TextField
                label="Title"
                variant="outlined"
                fullWidth
                required
                value={title}
                onChange={(e) => {
                  setTitle(e.target.value);
                  if (formErrors.title) {
                    setFormErrors((prev) => ({ ...prev, title: '' }));
                  }
                }}
                error={!!formErrors.title}
                helperText={formErrors.title}
                disabled={saving || success}
              />

              <TextField
                label="Content"
                variant="outlined"
                fullWidth
                required
                multiline
                rows={12}
                value={content}
                onChange={(e) => {
                  setContent(e.target.value);
                  if (formErrors.content) {
                    setFormErrors((prev) => ({ ...prev, content: '' }));
                  }
                }}
                error={!!formErrors.content}
                helperText={formErrors.content}
                disabled={saving || success}
              />

              <Box sx={{ mt: 2 }}>
                <Typography variant="subtitle1" gutterBottom>
                  Attachment (Optional)
                </Typography>
                <FileUpload
                  onFileSelect={handleFileSelect}
                  onRemove={handleRemoveFile}
                  uploadProgress={uploadProgress}
                  uploadedFileUrl={uploadedFileUrl || undefined}
                  error={uploadError || undefined}
                />
              </Box>

              <Box sx={{ display: 'flex', justifyContent: 'space-between', mt: 2 }}>
                <Button
                  variant="outlined"
                  color="secondary"
                  startIcon={<CancelIcon />}
                  onClick={handleCancel}
                  disabled={saving || success}
                >
                  Cancel
                </Button>
                <Button
                  type="submit"
                  variant="contained"
                  color="primary"
                  startIcon={saving ? <CircularProgress size={20} /> : <SaveIcon />}
                  disabled={saving || success}
                >
                  {saving ? 'Saving...' : 'Save Changes'}
                </Button>
              </Box>
            </Stack>
          </Box>
        </Paper>
      </Box>
    </Container>
  );
};

export default EditPostPage;

