package ru.kata.spring.boot_security.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.kata.spring.boot_security.demo.entity.User;
import ru.kata.spring.boot_security.demo.service.RoleService;
import ru.kata.spring.boot_security.demo.service.UserService;

import javax.validation.Valid;
import java.util.List;


@Controller
@RequestMapping("/admin")
public class AdminController {

    private final UserService userService;
    private final RoleService roleService;

    @Autowired
    public AdminController(UserService userService, RoleService roleService) {
        this.userService = userService;
        this.roleService = roleService;
    }

    @GetMapping()
    public String adminPage(Model model, @AuthenticationPrincipal User authUser) {
        model.addAttribute("authUser", authUser);
        model.addAttribute("users", userService.getAllUsers());
        model.addAttribute("user", new User());
        model.addAttribute("roles", roleService.getAllRoles());
        model.addAttribute("activeTab", "users");
        return "admin";
    }

    @GetMapping("/edit/{id}")
    public String showEditUserForm(@PathVariable Long id, Model model) {
        userService.findById(id).ifPresent(user -> {
            model.addAttribute("user", user); // Передаем объект пользователя в модель
            model.addAttribute("roles", roleService.getAllRoles()); // Передаем список ролей
        });
        return "admin"; // Возвращаем страницу admin.html
    }

    @GetMapping("/change-password/{username}")
    public String showChangePasswordForm(@PathVariable String username, Model model) {
        userService.findByUsername(username)
                .ifPresent(user -> model.addAttribute("user", user));
        return "change-password";
    }

    @ModelAttribute("authUser")
    public User getAuthUser(@AuthenticationPrincipal User authUser) {
        return authUser;
    }

    @PostMapping("/change-password/{username}")
    public String changePassword(@PathVariable String username,
                                 @RequestParam("password") String password,
                                 @RequestParam("confirmPassword") String confirmPassword,
                                 Model model) {
        if (!password.equals(confirmPassword)) {
            model.addAttribute("error", "Пароли не совпадают!");
            return "change-password";
        }

        userService.changePassword(username, password);
        return "redirect:/admin";
    }


    @GetMapping("/new")
    public String showNewUserForm(Model model) {
        model.addAttribute("user", new User());
        model.addAttribute("roles", roleService.getAllRoles());
        return "user-form";
    }

    @PostMapping("/save")
    public String saveUser(@Valid @ModelAttribute("user") User user,
                           BindingResult bindingResult,
                           @RequestParam(value = "roleIds", required = false) List<Long> roleIds,
                           RedirectAttributes redirectAttributes,
                           Model model,
                           @AuthenticationPrincipal User authUser) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("roles", roleService.getAllRoles());
            model.addAttribute("activeTab", "newUser"); // Устанавливаем активную вкладку
            return "admin"; // Возвращаем страницу с ошибками
        }

        if (userService.existsByUsername(user.getUsername(), user.getId())) {
            bindingResult.rejectValue("username", "error.user", "Этот логин уже используется!");
            model.addAttribute("roles", roleService.getAllRoles());
            model.addAttribute("activeTab", "newUser"); // Устанавливаем активную вкладку
            return "admin";
        }

        userService.saveUser(user, roleIds);
        redirectAttributes.addFlashAttribute("authUser", authUser);
        redirectAttributes.addFlashAttribute("clearTab", true);
        return "redirect:/admin";
    }


    @PatchMapping("/user/{id}")
    public String updateUser(@PathVariable Long id,
                             @Valid @ModelAttribute("user") User user,
                             BindingResult bindingResult,
                             @RequestParam(value = "roleIds", required = false) List<Long> roleIds,
                             RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            // Передаем ошибки валидации и ID пользователя для открытия модального окна
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.user", bindingResult);
            redirectAttributes.addFlashAttribute("user", user);
            redirectAttributes.addFlashAttribute("openEditModal", true); // Флаг для открытия модального окна
            return "redirect:/admin";
        }

        userService.updateUser(id, user, roleIds);
        redirectAttributes.addFlashAttribute("message", "Пользователь успешно обновлен!");
        return "redirect:/admin";
    }

    @DeleteMapping("/{id}")
    public String deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return "redirect:/admin";
    }
}
