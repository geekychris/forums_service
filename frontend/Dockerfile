# Build stage
FROM node:18-alpine AS build

# Set working directory
WORKDIR /app

# Copy package files and install dependencies
COPY package.json package-lock.json ./
RUN npm ci

# Copy source code
COPY . .

# Build the application
RUN npm run build

# Generate env config script for runtime environment variables
RUN echo "#!/bin/sh" > /app/env.sh && \
    echo "echo \"window.env = {" >> /app/env.sh && \
    echo "  REACT_APP_API_URL: '\${REACT_APP_API_URL:-}'" >> /app/env.sh && \
    echo "};\" > /usr/share/nginx/html/env-config.js" >> /app/env.sh && \
    chmod +x /app/env.sh

# Production stage
FROM nginx:alpine

# Copy built files from build stage
COPY --from=build /app/build /usr/share/nginx/html
COPY --from=build /app/env.sh /docker-entrypoint.d/40-env.sh

# Copy custom nginx configuration
RUN mkdir -p /etc/nginx/templates
COPY nginx.conf /etc/nginx/templates/default.conf.template

# Set environment variables for nginx template
ENV NGINX_HOST=localhost
ENV NGINX_PORT=80

# Add security headers
RUN echo 'add_header X-Frame-Options "SAMEORIGIN";' > /etc/nginx/conf.d/security-headers.conf && \
    echo 'add_header X-Content-Type-Options "nosniff";' >> /etc/nginx/conf.d/security-headers.conf && \
    echo 'add_header X-XSS-Protection "1; mode=block";' >> /etc/nginx/conf.d/security-headers.conf && \
    echo 'add_header Referrer-Policy "strict-origin-when-cross-origin";' >> /etc/nginx/conf.d/security-headers.conf && \
    echo 'add_header Content-Security-Policy "default-src \'self\'; img-src \'self\' data: https:; style-src \'self\' \'unsafe-inline\'; script-src \'self\' \'unsafe-inline\' \'unsafe-eval\'; connect-src \'self\' https:;";' >> /etc/nginx/conf.d/security-headers.conf

# Expose port
EXPOSE 80

# Set healthcheck
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 CMD wget --quiet --tries=1 --spider http://localhost || exit 1

# Optimize for production
RUN rm -rf /var/cache/apk/* && \
    rm -rf /tmp/*

# Run with unprivileged user
RUN addgroup -g 1001 -S appgroup && \
    adduser -u 1001 -S appuser -G appgroup

USER appuser

CMD ["nginx", "-g", "daemon off;"]

