package com.forum.app;

/**
 * Generic response wrapper for API calls.
 * Encapsulates success/failure status and provides error handling.
 *
 * @param <T> The type of data in the response
 */
public class ApiResponse<T> {
    
    private final boolean success;
    private final T data;
    private final String errorMessage;
    
    /**
     * Create a successful response.
     * 
     * @param data The response data
     */
    private ApiResponse(T data) {
        this.success = true;
        this.data = data;
        this.errorMessage = null;
    }
    
    /**
     * Create an error response.
     * 
     * @param errorMessage The error message
     */
    private ApiResponse(String errorMessage) {
        this.success = false;
        this.data = null;
        this.errorMessage = errorMessage;
    }
    
    /**
     * Create a successful response.
     * 
     * @param <T> The type of data
     * @param data The response data
     * @return A successful response containing the data
     */
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(data);
    }
    
    /**
     * Create an error response.
     * 
     * @param <T> The type of data
     * @param errorMessage The error message
     * @return An error response with the specified message
     */
    public static <T> ApiResponse<T> error(String errorMessage) {
        return new ApiResponse<>(errorMessage);
    }
    
    /**
     * Check if the response was successful.
     * 
     * @return true if the response was successful, false otherwise
     */
    public boolean isSuccess() {
        return success;
    }
    
    /**
     * Get the response data.
     * 
     * @return The response data, or null if this is an error response
     */
    public T getData() {
        return data;
    }
    
    /**
     * Get the error message.
     * 
     * @return The error message, or null if this is a successful response
     */
    public String getErrorMessage() {
        return errorMessage;
    }
}

