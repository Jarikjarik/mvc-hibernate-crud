package web.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import web.model.User;
import web.service.UserService;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/users")
public class UserController {
    private static final int DEFAULT_PAGE_SIZE = 5;

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public String getAllUsers(@RequestParam(value = "q", required = false) String searchTerm,
                              @RequestParam(value = "sort", defaultValue = "id") String sortBy,
                              @RequestParam(value = "dir", defaultValue = "asc") String sortDirection,
                              @RequestParam(value = "page", defaultValue = "1") int page,
                              Model model) {
        int safePage = Math.max(page, 1);
        long totalUsers = userService.countUsers(searchTerm);
        int totalPages = Math.max(1, (int) Math.ceil((double) totalUsers / DEFAULT_PAGE_SIZE));
        if (safePage > totalPages) {
            safePage = totalPages;
        }

        List<User> users = userService.getUsers(searchTerm, sortBy, sortDirection, safePage - 1, DEFAULT_PAGE_SIZE);
        model.addAttribute("users", users);
        model.addAttribute("searchTerm", searchTerm == null ? "" : searchTerm.trim());
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortDirection", sortDirection);
        model.addAttribute("currentPage", safePage);
        model.addAttribute("totalUsers", totalUsers);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("hasPreviousPage", safePage > 1);
        model.addAttribute("hasNextPage", safePage < totalPages);
        model.addAttribute("previousPage", safePage - 1);
        model.addAttribute("nextPage", safePage + 1);
        model.addAttribute("reverseSortDirection", "asc".equalsIgnoreCase(sortDirection) ? "desc" : "asc");
        return "users";
    }

    @GetMapping("/new")
    public String createUserForm(Model model) {
        model.addAttribute("user", new User());
        return "adduser";
    }

    @PostMapping
    public String createUser(@ModelAttribute("user") @Valid User user,
                             BindingResult bindingResult,
                             RedirectAttributes redirectAttributes) {
        validateUniqueEmail(user, bindingResult);
        if (bindingResult.hasErrors()) {
            return "adduser";
        }

        userService.addUser(user);
        redirectAttributes.addFlashAttribute("successMessage", "User has been created successfully.");
        return "redirect:/users";
    }

    @GetMapping("/edit")
    public String editUserForm(@RequestParam("id") Long id, Model model) {
        return userService.findById(id)
                .map(user -> {
                    model.addAttribute("user", user);
                    return "edituser";
                })
                .orElseGet(() -> {
                    model.addAttribute("errorMessage", "User with id " + id + " was not found.");
                    return "error";
                });
    }

    @PostMapping("/edit")
    public String editUser(@ModelAttribute("user") @Valid User user,
                           BindingResult bindingResult,
                           RedirectAttributes redirectAttributes) {
        validateUniqueEmail(user, bindingResult);
        if (bindingResult.hasErrors()) {
            return "edituser";
        }

        userService.editUser(user);
        redirectAttributes.addFlashAttribute("successMessage", "User has been updated successfully.");
        return "redirect:/users";
    }

    @PostMapping("/delete")
    public String deleteUser(@RequestParam("id") Long id,
                             RedirectAttributes redirectAttributes) {
        userService.deleteUser(id);
        redirectAttributes.addFlashAttribute("successMessage", "User has been deleted successfully.");
        return "redirect:/users";
    }

    private void validateUniqueEmail(User user, BindingResult bindingResult) {
        if (bindingResult.hasFieldErrors("email")) {
            return;
        }

        Optional<User> existingUser = userService.findByEmail(user.getEmail());
        if (existingUser.isPresent() && !existingUser.get().getId().equals(user.getId())) {
            bindingResult.rejectValue("email", "email.exists", "Email is already in use.");
        }
    }
}
