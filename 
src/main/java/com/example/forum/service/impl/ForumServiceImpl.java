    @Transactional
    public Forum createSubforum(String name, String description, Long parentId, Long creatorId) {
        // Validate input
        if (name == null || name.trim().isEmpty()) {
            throw new BadRequestException("Forum name cannot be empty");
        }

        // Check if user has admin access to parent forum
        if (!hasForumAccess(parentId, creatorId, AccessLevel.ADMIN)) {
            throw new AccessDeniedException("forum", "create subforum in");
        }

        // Get creator user
        User creator = userService.getUserById(creatorId);
        
        // Get parent forum
        Forum parentForum = getForumById(parentId);
    @Transactional
    public Forum updateForum(Long id, String name, String description, Long userId) {
        // Check if user has admin access first
        if (!hasForumAccess(id, userId, AccessLevel.ADMIN)) {
            throw new AccessDeniedException("forum", "update");
        }

        // Get the forum
        Forum forum = getForumById(id);
    @Transactional
    public void deleteForum(Long id, Long userId) {
        // Check if user has admin access
        if (!hasForumAccess(id, userId, AccessLevel.ADMIN)) {
            throw new AccessDeniedException("forum", "delete");
        }
        
        // Get the forum
        Forum forum = getForumById(id);
    @Transactional
    public Forum moveForum(Long id, Long newParentId, Long userId) {
        // Check if user has admin access to the forum
        if (!hasForumAccess(id, userId, AccessLevel.ADMIN)) {
            throw new AccessDeniedException("forum", "move");
        }
        
        // Get the forum to move
        Forum forum = getForumById(id);
    @Transactional
    public boolean grantForumAccess(Long forumId, Long userId, AccessLevel accessLevel, Long granterId) {
        // Check if granter has admin access
        if (!hasForumAccess(forumId, granterId, AccessLevel.ADMIN)) {
            throw new AccessDeniedException("forum", "manage access to");
        }
        
        // Get forum and users
        Forum forum = getForumById(forumId);
        User user = userService.getUserById(userId);
    @Transactional
    public boolean revokeForumAccess(Long forumId, Long userId, Long revokerId) {
        }
        
        // If moving to root level
        if (newParentId == null) {
            // Check if a root forum with this name already exists
            Optional<Forum> existingForum = forumRepository.findByNameIgnoreCase(forum.getName());
            if (exi    @Transactional
    public boolean updateForumAccess(Long forumId, Long userId, AccessLevel accessLevel, Long updaterId) {
        // This is essentially the same as granting access, but we'll separate it for clarity in the API
        
        // Check if forum exists first
        if (!forumRepository.existsById(forumId)) {
            throw new ResourceNotFoundException("Forum", "id", forumId);
        }
        
        // Check if updater has admin access before proceeding
        if (!hasForumAccess(forumId, updaterId, AccessLevel.ADMIN)) {
            throw new AccessDeniedException("forum", "manage access to");
        }
        
        // Get forum and users after access check
        Forum forum = getForumById(forumId);
        User user = userService.getUserById(userId);
ck if revoker has admin access before proceeding
        if (!hasForumAccess(forumId, revokerId, AccessLevel.ADMIN)) {
            throw new AccessDeniedException("forum", "manage access to");
        }
        
        // Get forum after access check
        Forum forum = getForumById(forumId);

        
        // Check if granter has admin access before proceeding
        if (!hasForumAccess(forumId, granterId, AccessLevel.ADMIN)) {
            throw new AccessDeniedException("forum", "manage access to");
        }
        
        // Get forum and users after access check
        Forum forum = getForumById(forumId);
        User user = userService.getUserById(userId);
if (!hasForumAccess(id, userId, AccessLevel.ADMIN)) {
            throw new AccessDeniedException("forum", "move");
        }

        // Check target forum access if moving to a parent
        if (newParentId != null) {
            if (!forumRepository.existsById(newParentId)) {
                throw new ResourceNotFoundException("Target forum", "id", newParentId);
            }
            if (!hasForumAccess(newParentId, userId, AccessLevel.ADMIN)) {
                throw new AccessDeniedException("target forum", "move to");
            }
        }

        // Get the forums after access checks
        Forum forum = getForumById(id);
        Forum newParent = newParentId != null ? getForumById(newParentId) : null;
      if (!hasForumAccess(id, userId, AccessLevel.ADMIN)) {
            throw new AccessDeniedException("forum", "delete");
        }

        // Get the forum after access check
        Forum forum = getForumById(id);
 admin access before proceeding
        if (!hasForumAccess(id, userId, AccessLevel.ADMIN)) {
            throw new AccessDeniedException("forum", "update");
        }

        // Get the forum after access check
        Forum forum = getForumById(id);
