package com.netkrow.backend.exception;

/**
 * - Formato estándar de error para FE:
 *   { "status": 400, "message": "..." }
 */
public class ErrorResponse {
    private int status;
    private String message;

    public ErrorResponse() {}

    public ErrorResponse(int status, String message) {
        this.status = status;
        this.message = message;
    }

    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
