package web.controller;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ControllerAdvice;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public String handleIllegalArgument(IllegalArgumentException exception, Model model) {
        model.addAttribute("errorMessage", exception.getMessage());
        return "error";
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public String handleDataIntegrityViolation(DataIntegrityViolationException exception, Model model) {
        model.addAttribute("errorMessage", "Unable to save the user because one of the fields violates database constraints.");
        return "error";
    }

    @ExceptionHandler(Exception.class)
    public String handleGenericException(Exception exception, Model model) {
        model.addAttribute("errorMessage", "Unexpected server error: " + exception.getClass().getSimpleName());
        return "error";
    }
}
