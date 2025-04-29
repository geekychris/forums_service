import React, { useState, useRef, ChangeEvent } from 'react';
import { 
  Box, 
  Button, 
  Typography, 
  LinearProgress,
  Alert,
  IconButton,
  Card,
  CardMedia,
  CardContent,
  Stack,
  Chip
} from '@mui/material';
import {
  CloudUpload as CloudUploadIcon,
  Close as CloseIcon,
  InsertDriveFile as FileIcon,
  Image as ImageIcon,
  PictureAsPdf as PdfIcon,
  VideoFile as VideoIcon,
  AudioFile as AudioIcon,
} from '@mui/icons-material';

const ALLOWED_FILE_TYPES = [
  // Images
  'image/jpeg', 'image/png', 'image/gif', 'image/webp',
  // Documents
  'application/pdf', 'application/msword', 'application/vnd.openxmlformats-officedocument.wordprocessingml.document',
  // Audio
  'audio/mpeg', 'audio/wav', 'audio/ogg',
  // Video
  'video/mp4', 'video/webm', 'video/quicktime'
];

interface FileUploadProps {
  onFileSelect: (file: File) => void;
  onUploadComplete?: (url: string) => void;
  onRemove?: () => void;
  uploadProgress?: number;
  uploadedFileUrl?: string;
  error?: string;
  maxFileSizeMB?: number;
  allowedTypes?: string[];
}

const FileUpload: React.FC<FileUploadProps> = ({
  onFileSelect,
  onUploadComplete,
  onRemove,
  uploadProgress = 0,
  uploadedFileUrl,
  error,
  maxFileSizeMB = 10,
  allowedTypes = ALLOWED_FILE_TYPES
}) => {
  const [selectedFile, setSelectedFile] = useState<File | null>(null);
  const [previewUrl, setPreviewUrl] = useState<string | null>(null);
  const [fileError, setFileError] = useState<string | null>(null);
  const fileInputRef = useRef<HTMLInputElement>(null);

  const maxSize = maxFileSizeMB * 1024 * 1024;

  const validateFile = (file: File): boolean => {
    // Check file size
    if (file.size > maxSize) {
      setFileError(`File size exceeds ${maxFileSizeMB}MB limit`);
      return false;
    }

    // Check file type
    if (!allowedTypes.includes(file.type)) {
      setFileError('File type not supported');
      return false;
    }

    return true;
  };

  const handleFileChange = (event: ChangeEvent<HTMLInputElement>) => {
    const file = event.target.files?.[0];
    if (!file) return;

    setFileError(null);

    if (validateFile(file)) {
      setSelectedFile(file);
      onFileSelect(file);

      // Create preview for images
      if (file.type.startsWith('image/')) {
        const reader = new FileReader();
        reader.onload = () => {
          setPreviewUrl(reader.result as string);
        };
        reader.readAsDataURL(file);
      } else {
        setPreviewUrl(null);
      }
    }

    // Reset input to allow selecting the same file again
    if (fileInputRef.current) {
      fileInputRef.current.value = '';
    }
  };

  const handleRemoveFile = () => {
    setSelectedFile(null);
    setPreviewUrl(null);
    setFileError(null);
    
    if (onRemove) {
      onRemove();
    }
  };

  const handleButtonClick = () => {
    fileInputRef.current?.click();
  };

  const renderFileIcon = (fileType: string) => {
    if (fileType.startsWith('image/')) return <ImageIcon />;
    if (fileType.startsWith('video/')) return <VideoIcon />;
    if (fileType.startsWith('audio/')) return <AudioIcon />;
    if (fileType === 'application/pdf') return <PdfIcon />;
    return <FileIcon />;
  };

  const formatFileSize = (bytes: number): string => {
    if (bytes < 1024) return bytes + ' bytes';
    if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB';
    return (bytes / (1024 * 1024)).toFixed(1) + ' MB';
  };

  return (
    <Box sx={{ my: 2 }}>
      <input
        type="file"
        ref={fileInputRef}
        onChange={handleFileChange}
        style={{ display: 'none' }}
        accept={allowedTypes.join(',')}
      />

      {fileError && (
        <Alert severity="error" sx={{ mb: 2 }}>
          {fileError}
        </Alert>
      )}

      {error && (
        <Alert severity="error" sx={{ mb: 2 }}>
          {error}
        </Alert>
      )}

      {!selectedFile && !uploadedFileUrl ? (
        <Button
          variant="outlined"
          startIcon={<CloudUploadIcon />}
          onClick={handleButtonClick}
          fullWidth
          sx={{ 
            height: 100, 
            border: '2px dashed #ccc',
            borderRadius: 2,
            display: 'flex',
            flexDirection: 'column',
            gap: 1
          }}
        >
          <Typography variant="body1">Upload File</Typography>
          <Typography variant="caption" color="text.secondary">
            Max size: {maxFileSizeMB}MB
          </Typography>
        </Button>
      ) : (
        <Card variant="outlined" sx={{ position: 'relative', mb: 2 }}>
          <IconButton
            size="small"
            onClick={handleRemoveFile}
            sx={{
              position: 'absolute',
              top: 8,
              right: 8,
              bgcolor: 'rgba(0,0,0,0.2)',
              color: 'white',
              '&:hover': {
                bgcolor: 'rgba(0,0,0,0.5)',
              },
              zIndex: 1,
            }}
          >
            <CloseIcon fontSize="small" />
          </IconButton>

          {previewUrl && selectedFile?.type.startsWith('image/') ? (
            <CardMedia
              component="img"
              image={previewUrl}
              alt="Preview"
              sx={{ height: 200, objectFit: 'contain' }}
            />
          ) : (
            <Box
              sx={{
                height: 120,
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                bgcolor: 'background.default',
              }}
            >
              {renderFileIcon(selectedFile?.type || '')}
            </Box>
          )}

          <CardContent>
            <Stack spacing={1}>
              <Typography variant="body2" noWrap>
                {selectedFile?.name || 'Uploaded file'}
              </Typography>
              
              <Stack direction="row" spacing={1}>
                <Chip 
                  label={selectedFile ? formatFileSize(selectedFile.size) : ''} 
                  size="small" 
                  variant="outlined"
                />
                <Chip 
                  label={selectedFile?.type.split('/')[1] || ''} 
                  size="small" 
                  variant="outlined"
                />
              </Stack>

              {uploadProgress > 0 && uploadProgress < 100 && (
                <Box sx={{ width: '100%', mt: 1 }}>
                  <LinearProgress variant="determinate" value={uploadProgress} />
                  <Typography variant="caption" align="center" display="block" sx={{ mt: 0.5 }}>
                    {`${Math.round(uploadProgress)}%`}
                  </Typography>
                </Box>
              )}

              {uploadedFileUrl && (
                <Button size="small" href={uploadedFileUrl} target="_blank">
                  View File
                </Button>
              )}
            </Stack>
          </CardContent>
        </Card>
      )}
    </Box>
  );
};

export default FileUpload;

