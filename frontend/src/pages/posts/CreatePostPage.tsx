import React, { useState, useEffect } from 'react';
import { 
  Typography, 
  Box, 
  TextField, 
  Button, 
  Paper, 
  Alert, 
  CircularProgress,
  Divider,
  FormControl,
  InputLabel,
  MenuItem,
  Select,
  SelectChangeEvent
} from '@mui/material';
import { useNavigate, useParams } from 'react-router-dom';
import api from '../../services/api';
import { CreatePostRequest, Forum } from '../../types';
import FileUpload from '../../components/common/FileUpload';

const CreatePostPage: React.FC = () => {
  const { forumId } = useParams<{ forumId: string }>();
  const navigate = useNavigate();
  const [formData, setFormData] = useState<CreatePostRequest>({
    title: '',
    content: '',
    forumId: forumId ? parseInt(forumId, 10) : 0
  });
  const [errors, setErrors] = useState<Record<string, string>>({});
  const [loading, setLoading] = useState<boolean>(false);
  const [uploadedFileUrl, setUploadedFileUrl] = useState<string | null>(null);
  const [uploadProgress, setUploadProgress] = useState<number>(0);
  const [uploadError, setUploadError] = useState<string | null>(null);
  const [selectedFile, setSelectedFile] = useState<File | null>(null);
  const [forums, setForums] = useState<Forum[]>([]);
  const [fetchingForums, setFetchingForums] = useState<boolean>(false);
  const [error, setError] = useState<string | null>(null);
  const [forum, setForum] = useState<Forum | null>(null);

  useEffect(() => {
    // If forumId is not provided in URL, fetch all forums for dropdown
    if (!forumId) {
      const fetchForums = async () => {
        try {
          setFetchingForums(true);
          const fetchedForums = await api.getForums();
          setForums(fetchedForums);
        } catch (err) {
          console.error('Error fetching forums:', err);
          setError('Failed to load forums. Please try again later.');
        } finally {
          setFetchingForums(false);
        }
      };

      fetchForums();
    } else {
      // If forumId is provided, fetch forum details
      const fetchForum = async () => {
        try {
          const fetchedForum = await api.getForum(parseInt(forumId, 10));
          setForum(fetchedForum);
        } catch (err) {
          console.error('Error fetching forum details:', err);
          setError('Failed to load forum details. Please try again later.');
        }
      };

      fetchForum();
    }
  }, [forumId]);

  const validateForm = (): boolean => {
    const newErrors: Record<string, string> = {};
    
    if (!formData.title.trim()) {
      newErrors.title = 'Title is required';
    } else if (formData.title.length < 5) {
      newErrors.title = 'Title must be at least 5 characters';
    } else if (formData.title.length > 100) {
      newErrors.title = 'Title must be less than 100 characters';
    }
    
    if (!formData.content.trim()) {
      newErrors.content = 'Content is required';
    } else if (formData.content.length < 10) {
      newErrors.content = 'Content must be at least 10 characters';
    } else if (formData.content.length > 10000) {
      newErrors.content = 'Content must be less than 10,000 characters';
    }

    if (!forumId && !formData.forumId) {
      newErrors.forumId = 'Please select a forum';
    }
    
    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));
    
    // Clear error when user types
    if (errors[name]) {
      setErrors(prev => ({
        ...prev,
        [name]: ''
      }));
    }
  };

  const handleForumChange = (e: SelectChangeEvent<number>) => {
    const value = e.target.value as number;
    setFormData(prev => ({
      ...prev,
      forumId: value
    }));
    
    if (errors.forumId) {
      setErrors(prev => ({
        ...prev,
        forumId: ''
      }));
    }
  };

  const handleFileSelect = (file: File) => {
    setSelectedFile(file);
    setUploadError(null);
    setUploadProgress(0);
  };

  const uploadFile = async (): Promise<string | null> => {
    if (!selectedFile) return null;
    
    try {
      // In a real implementation, you would track upload progress here
      // For this example, we'll simulate progress
      setUploadProgress(10);
      setTimeout(() => setUploadProgress(30), 500);
      setTimeout(() => setUploadProgress(60), 1000);
      setTimeout(() => setUploadProgress(90), 1500);
      
      const fileUrl = await api.uploadFile(selectedFile);
      setUploadProgress(100);
      setUploadedFileUrl(fileUrl);
      return fileUrl;
    } catch (err) {
      console.error('Error uploading file:', err);
      setUploadError('Failed to upload file. Please try again.');
      return null;
    }
  };
  
  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    
    if (!validateForm()) {
      return;
    }
    
    setLoading(true);
    setError(null);
    
    // Upload file first if selected
    let fileUrl = null;
    if (selectedFile) {
      fileUrl = await uploadFile();
      if (!fileUrl && uploadError) {
        setLoading(false);
        return;
      }
    }
    
    // Now create the post
    try {
      // Add file URL to content if available
      const enhancedContent = fileUrl 
        ? `${formData.content}\n\n[Attached file](${fileUrl})`
        : formData.content;
      
      const updatedFormData = {
        ...formData,
        content: enhancedContent
      };
      
      const newPost = await api.createPost(updatedFormData);
      
      // Navigate to the new post page
      navigate(`/posts/${newPost.id}`);
    } catch (err) {
      console.error('Error creating post:', err);
      setError('Failed to create post. Please try again later.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div>
      <Typography variant="h4" component="h1" gutterBottom>
        Create New Post
      </Typography>
      
      {forum && (
        <Typography variant="h6" color="text.secondary" gutterBottom>
          in {forum.name}
        </Typography>
      )}
      
      <Paper elevation={1} sx={{ p: 3, mt: 3, borderRadius: 2 }}>
        {error && (
          <Alert severity="error" sx={{ mb: 3 }}>
            {error}
          </Alert>
        )}
        
        <form onSubmit={handleSubmit}>
          {!forumId && (
            <FormControl 
              fullWidth 
              margin="normal" 
              error={!!errors.forumId}
              disabled={loading || fetchingForums}
            >
              <InputLabel id="forum-select-label">Forum</InputLabel>
              <Select
                labelId="forum-select-label"
                id="forum-select"
                value={formData.forumId || ''}
                label="Forum"
                onChange={handleForumChange}
              >
                {fetchingForums ? (
                  <MenuItem disabled>Loading forums...</MenuItem>
                ) : forums.length === 0 ? (
                  <MenuItem disabled>No forums available</MenuItem>
                ) : (
                  forums.map((forum) => (
                    <MenuItem key={forum.id} value={forum.id}>{forum.name}</MenuItem>
                  ))
                )}
              </Select>
              {errors.forumId && (
                <Typography variant="caption" color="error">
                  {errors.forumId}
                </Typography>
              )}
            </FormControl>
          )}
          
          <TextField
            fullWidth
            label="Title"
            name="title"
            value={formData.title}
            onChange={handleChange}
            error={!!errors.title}
            helperText={errors.title}
            margin="normal"
            required
            disabled={loading}
            inputProps={{ maxLength: 100 }}
          />
          
          <TextField
            fullWidth
            label="Content"
            name="content"
            value={formData.content}
            onChange={handleChange}
            error={!!errors.content}
            helperText={errors.content}
            margin="normal"
            required
            multiline
            rows={8}
            disabled={loading}
            inputProps={{ maxLength: 10000 }}
          />
          
          <Typography variant="subtitle1" sx={{ mt: 3, mb: 1 }}>
            Attach File (Optional)
          </Typography>
          
          <FileUpload
            onFileSelect={handleFileSelect}
            uploadProgress={uploadProgress}
            uploadedFileUrl={uploadedFileUrl || undefined}
            error={uploadError || undefined}
          />
          
          <Divider sx={{ my: 3 }} />
          
          <Box sx={{ 
            display: 'flex', 
            justifyContent: 'space-between', 
            mt: 3 
          }}>
            <Button 
              variant="outlined" 
              onClick={() => navigate(forumId ? `/forums/${forumId}` : '/forums')}
              disabled={loading}
            >
              Cancel
            </Button>
            <Button 
              type="submit" 
              variant="contained" 
              color="primary"
              disabled={loading}
              startIcon={loading ? <CircularProgress size={20} color="inherit" /> : null}
            >
              {loading ? 'Creating...' : 'Create Post'}
            </Button>
          </Box>
        </form>
      </Paper>
    </div>
  );
};

export default CreatePostPage;

