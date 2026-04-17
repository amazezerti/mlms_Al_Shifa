package com.alshifa.mlms_al_shifa.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@ControllerAdvice
public class GlobalExceptionHandler {

    // ── 404 — Page / Resource Not Found ──────────────────────────
    @ExceptionHandler(NoResourceFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleNoResource(
            NoResourceFoundException ex,
            HttpServletRequest request,
            Model model) {
        model.addAttribute("errorCode",    "404");
        model.addAttribute("errorTitle",   "Page Not Found");
        model.addAttribute("errorMessage",
                "The page you are looking for does not exist "
                        + "or has been moved.");
        model.addAttribute("requestedUrl",
                request.getRequestURI());
        return "error/error";
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleNoHandler(
            NoHandlerFoundException ex,
            HttpServletRequest request,
            Model model) {
        model.addAttribute("errorCode",    "404");
        model.addAttribute("errorTitle",   "Page Not Found");
        model.addAttribute("errorMessage",
                "The page '"
                        + ex.getRequestURL()
                        + "' does not exist.");
        model.addAttribute("requestedUrl",
                request.getRequestURI());
        return "error/error";
    }

    // ── 400 — Validation errors ───────────────────────────────────
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleValidation(
            MethodArgumentNotValidException ex,
            Model model) {
        StringBuilder sb = new StringBuilder();
        ex.getBindingResult().getFieldErrors().forEach(err ->
                sb.append(err.getField())
                        .append(": ")
                        .append(err.getDefaultMessage())
                        .append(". ")
        );
        model.addAttribute("errorCode",    "400");
        model.addAttribute("errorTitle",   "Validation Error");
        model.addAttribute("errorMessage", sb.toString());
        model.addAttribute("requestedUrl", "");
        return "error/error";
    }

    // ── IllegalArgumentException from service layer ───────────────
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleIllegalArg(
            IllegalArgumentException ex,
            HttpServletRequest request,
            Model model) {
        model.addAttribute("errorCode",    "400");
        model.addAttribute("errorTitle",   "Invalid Request");
        model.addAttribute("errorMessage", ex.getMessage());
        model.addAttribute("requestedUrl",
                request.getRequestURI());
        return "error/error";
    }
}