import React, { useState, useEffect, useRef } from 'react';
import { 
  Typography, 
  Box, 
  Button, 
  Paper, 
  Divider, 
  Avatar, 
  Card,
  CardContent,
  CardMedia,
  TextField,
  Chip,
  Alert,
  Skeleton,
  Grid
} from '@mui/material';
import { 
  Reply as ReplyIcon,
  Link as LinkIcon,
  Send as SendIcon,
  Close as CloseIcon,
  InsertDriveFile as FileIcon
} from '@mui/icons-material';
import { useParams, Link as RouterLink } from 'react-router-dom';
import api from '../../services/api';
import { Post, Comment } from '../../types';
import { format } from 'date-fns';

// Define attachment interface
interface Attachment {
  url: string;
  filename: string;
  type: 'image' | 'file';
}

// Define CommentItem props interface
interface CommentItemProps {
  comment: Comment;
  postId: number;
  depth?: number;
  onReply: (parentId: number) => void;
}

// Component for displaying a single comment
const CommentItem: React.FC<CommentItemProps> = ({ 
  comment, 
  postId,
  depth = 0,
  onReply 
}) => {
  return (
    <Paper sx={{ p: 2, mb: 2, ml: depth * 4 }}>
      <Box sx={{ display: 'flex', alignItems: 'center', mb: 1 }}>
        <Avatar 
          alt={comment.author.username} 
          src={`https://ui-avatars.com/api/?name=${comment.author.username}&background=random`} 
          sx={{ width: 36, height: 36, mr: 1.5 }}
        />
        <Box>
          <Typography variant="subtitle2">
            {comment.author.username}
          </Typography>
          <Typography variant="caption" color="text.secondary">
            {format(new Date(comment.createdAt), 'MMMM d, yyyy h:mm a')}
          </Typography>
        </Box>
      </Box>
      
      <Typography variant="body2" sx={{ mt: 1, mb: 2, whiteSpace: 'pre-line' }}>
        {comment.content}
      </Typography>
      
      <Box sx={{ display: 'flex', justifyContent: 'flex-end' }}>
        <Button
          size="small"
          startIcon={<ReplyIcon />}
          onClick={() => onReply(comment.id)}
        >
          Reply
        </Button>
      </Box>
      
      {comment.replies && comment.replies.length > 0 && (
        <Box sx={{ mt: 2 }}>
          {comment.replies.map(reply => (
            <CommentItem
              key={reply.id}
              comment={reply}
              postId={postId}
              depth={depth + 1}
              onReply={onReply}
            />
          ))}
        </Box>
      )}
    </Paper>
  );
};
// Main component for the post detail page
const PostDetailPage: React.FC = () => {
  const { postId } = useParams<{ postId: string }>();
  const [post, setPost] = useState<Post | null>(null);
  const [comments, setComments] = useState<Comment[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [newComment, setNewComment] = useState('');
  const [commentError, setCommentError] = useState<string | null>(null);
  const [submittingComment, setSubmittingComment] = useState(false);
  const [replyTo, setReplyTo] = useState<number | null>(null);
  const commentSectionRef = useRef<HTMLDivElement>(null);
  const isAuthenticated = localStorage.getItem('token') !== null;

  useEffect(() => {
    const fetchPostAndComments = async () => {
      if (!postId) return;
      
      setLoading(true);
      try {
        // Fetch both post and comments data
        const [postData, commentsData] = await Promise.all([
          api.getPost(parseInt(postId, 10)),
          api.getPostComments(parseInt(postId, 10))
        ]);
        
        setPost(postData);
        setComments(commentsData.content);
        setError(null);
      } catch (err) {
        console.error('Error fetching post details:', err);
        setError('Failed to load post. Please try again later.');
      } finally {
        setLoading(false);
      }
    };

    fetchPostAndComments();
  }, [postId]);

  const handleNewCommentChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
    setNewComment(e.target.value);
    if (commentError) {
      setCommentError(null);
    }
  };

  const handleSubmitComment = async (e: React.FormEvent) => {
    e.preventDefault();
    
    if (!newComment.trim()) {
      setCommentError('Comment cannot be empty');
      return;
    }
    
    if (!postId) return;
    
    setSubmittingComment(true);
    
    try {
      const commentData = {
        content: newComment,
        postId: parseInt(postId, 10),
        ...(replyTo ? { parentCommentId: replyTo } : {})
      };
      
      const createdComment = await api.createComment(commentData);
      
      // Update comments list
      if (replyTo) {
        // Find the parent comment and add the reply
        setComments(prevComments => {
          return prevComments.map(comment => {
            if (comment.id === replyTo) {
              return {
                ...comment,
                replies: [...(comment.replies || []), createdComment]
              };
            }
            return comment;
          });
        });
      } else {
        // Add as a top-level comment
        setComments(prevComments => [...prevComments, createdComment]);
      }
      
      // Clear form
      setNewComment('');
      setReplyTo(null);
      
      // Scroll to the newly added comment
      setTimeout(() => {
        commentSectionRef.current?.scrollIntoView({ behavior: 'smooth' });
      }, 100);
      
    } catch (err) {
      console.error('Error creating comment:', err);
      setCommentError('Failed to post comment. Please try again.');
    } finally {
      setSubmittingComment(false);
    }
  };

  const handleReply = (parentId: number) => {
    setReplyTo(parentId);
    
    // Scroll to comment form
    setTimeout(() => {
      document.getElementById('comment-form')?.scrollIntoView({ behavior: 'smooth' });
    }, 100);
  };

  const handleCancelReply = () => {
    setReplyTo(null);
  };

  const formatDate = (dateString: string) => {
    return format(new Date(dateString), 'MMMM d, yyyy h:mm a');
  };

  // Function to extract file attachments from content
  const extractAttachments = (content: string): { content: string, attachments: Attachment[] } => {
    const attachmentRegex = /\[Attached file\]\((https?:\/\/[^\s)]+)\)/g;
    const attachments: Attachment[] = [];
    let match;
    
    // Find all attachments
    while ((match = attachmentRegex.exec(content)) !== null) {
      const url = match[1];
      const isImage = !!url.match(/\.(jpg|jpeg|png|gif|webp)$/i);
      attachments.push({
        url,
        filename: url.split('/').pop() || `Attachment ${attachments.length + 1}`,
        type: isImage ? 'image' : 'file'
      });
    }
    
    // Remove attachment links from content
    const cleanContent = content.replace(attachmentRegex, '').trim();
    
    return { content: cleanContent, attachments };
  };

  return (
    <div>
      {error && (
        <Alert severity="error" sx={{ mb: 3 }}>
          {error}
        </Alert>
      )}

      {loading && !post ? (
        // Skeleton for post loading
        <Box>
          <Skeleton animation="wave" height={60} width="80%" />
          <Skeleton animation="wave" height={30} width="40%" sx={{ mt: 1 }} />
          <Skeleton animation="wave" height={200} sx={{ mt: 3 }} />
        </Box>
      ) : post ? (
        // Post content
        <>
          <Paper elevation={1} sx={{ p: 3, borderRadius: 2 }}>
            <Typography variant="h4" component="h1" gutterBottom>
              {post.title}
            </Typography>
            
            <Box sx={{ display: 'flex', alignItems: 'center', mb: 3 }}>
              <Avatar 
                alt={post.author.username} 
                src={`https://ui-avatars.com/api/?name=${post.author.username}&background=random`} 
                sx={{ width: 40, height: 40, mr: 2 }}
              />
              <Box>
                <Typography variant="subtitle1">
                  {post.author.username}
                </Typography>
                <Typography variant="caption" color="text.secondary">
                  {formatDate(post.createdAt)} in{' '}
                  <RouterLink to={`/forums/${post.forum.id}`} style={{ textDecoration: 'none' }}>
                    <Chip label={post.forum.name} size="small" clickable />
                  </RouterLink>
                </Typography>
              </Box>
            </Box>
            
            <Divider sx={{ mb: 3 }} />
            
            {post.content && (() => {
              const { content, attachments } = extractAttachments(post.content);
              return (
                <>
                  <Typography variant="body1" sx={{ mb: 3, whiteSpace: 'pre-line' }}>
                    {content}
                  </Typography>
                  
                  {/* Render attachments if any */}
                  {attachments.length > 0 && (
                    <Box sx={{ mb: 3 }}>
                      <Typography variant="subtitle1" sx={{ mb: 1 }}>
                        Attachments:
                      </Typography>
                      <Grid container spacing={2}>
                        {attachments.map((attachment, index) => (
                          <Grid item xs={12} sm={6} md={4} key={index}>
                            <Card variant="outlined">
                              {attachment.type === 'image' ? (
                                <CardMedia
                                  component="img"
                                  height="140"
                                  image={attachment.url}
                                  alt={attachment.filename}
                                  sx={{ objectFit: 'contain' }}
                                />
                              ) : (
                                <Box sx={{ p: 2, display: 'flex', alignItems: 'center' }}>
                                  <FileIcon fontSize="large" sx={{ mr: 1 }} />
                                  <Box sx={{ flex: 1, overflow: 'hidden' }}>
                                    <Typography variant="body2" noWrap>
                                      {attachment.filename}
                                    </Typography>
                                  </Box>
                                </Box>
                              )}
                              <CardContent sx={{ p: 1 }}>
                                <Button 
                                  size="small" 
                                  fullWidth 
                                  href={attachment.url} 
                                  target="_blank"
                                  startIcon={<LinkIcon />}
                                >
                                  {attachment.type === 'image' ? 'View Image' : 'Download File'}
                                </Button>
                              </CardContent>
                            </Card>
                          </Grid>
                        ))}
                      </Grid>
                    </Box>
                  )}
                </>
              );
            })()}
          </Paper>

          {/* Comments Section */}
          <Box sx={{ mt: 4 }} ref={commentSectionRef}>
            <Typography variant="h5" component="h2" gutterBottom>
              Comments ({comments.length})
            </Typography>
            
            {comments.length === 0 ? (
              <Paper elevation={0} sx={{ p: 3, textAlign: 'center', bgcolor: 'background.default' }}>
                <Typography variant="body1" color="text.secondary">
                  No comments yet. Be the first to comment!
                </Typography>
              </Paper>
            ) : (
              <Box sx={{ mt: 2 }}>
                {comments.map(comment => (
                  <CommentItem 
                    key={comment.id} 
                    comment={comment} 
                    postId={parseInt(postId || '0', 10)}
                    onReply={handleReply}
                  />
                ))}
              </Box>
            )}
            
            {/* Comment Form */}
            {isAuthenticated && (
              <Paper elevation={1} sx={{ p: 3, mt: 4, borderRadius: 2 }} id="comment-form">
                <Typography variant="h6" component="h3" gutterBottom>
                  {replyTo ? 'Reply to comment' : 'Add a comment'}
                </Typography>
                
                {replyTo && (
                  <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
                    <Chip 
                      label="Replying to a comment" 
                      color="primary" 
                      variant="outlined" 
                      onDelete={handleCancelReply}
                      deleteIcon={<CloseIcon />}
                    />
                  </Box>
                )}
                
                {commentError && (
                  <Alert severity="error" sx={{ mb: 2 }}>
                    {commentError}
                  </Alert>
                )}
                
                <form onSubmit={handleSubmitComment}>
                  <TextField
                    fullWidth
                    multiline
                    rows={4}
                    value={newComment}
                    onChange={handleNewCommentChange}
                    error={!!commentError}
                    helperText={commentError}
                    disabled={submittingComment}
                    placeholder="Write your comment here..."
                    variant="outlined"
                  />
                  
                  <Box sx={{ mt: 2, display: 'flex', justifyContent: 'flex-end' }}>
                    {replyTo && (
                      <Button
                        onClick={handleCancelReply}
                        disabled={submittingComment}
                        sx={{ mr: 2 }}
                      >
                        Cancel
                      </Button>
                    )}
                    <Button
                      type="submit"
                      variant="contained"
                      disabled={submittingComment}
                      endIcon={<SendIcon />}
                    >
                      {submittingComment ? 'Posting...' : 'Post Comment'}
                    </Button>
                  </Box>
                </form>
              </Paper>
            )}
          </Box>
        </>
      ) : null}
    </div>
  );
};

export default PostDetailPage;
