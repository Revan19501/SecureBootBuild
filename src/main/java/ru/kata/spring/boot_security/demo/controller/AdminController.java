package ru.kata.spring.boot_security.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
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
    public String adminPage(Model model) {
        model.addAttribute("users", userService.getAllUsers());
        model.addAttribute("user", new User());
        model.addAttribute("allRoles", roleService.getAllRoles());
        model.addAttribute("activeTab", "users");
        return "admin";
    }

    @GetMapping("/edit/{username}")
    public String showEditUserForm(@PathVariable String username, Model model) {
        return userService.findByUsername(username)
                .map(user -> {
                    model.addAttribute("user", user);
                    model.addAttribute("allRoles", roleService.getAllRoles());
                    return "user-form";
                })
                .orElse("redirect:/admin");
    }

    @GetMapping("/change-password/{username}")
    public String showChangePasswordForm(@PathVariable String username, Model model) {
        userService.findByUsername(username)
                .ifPresent(user -> model.addAttribute("user", user));
        return "change-password";
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
        model.addAttribute("allRoles", roleService.getAllRoles());
        return "user-form";
    }

    @PostMapping("/save")
    public String saveUser(@Valid @ModelAttribute("user") User user,
                           BindingResult bindingResult,
                           @RequestParam(value = "roleIds", required = false) List<Long> roleIds,
                           Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("allRoles", roleService.getAllRoles());
            return "user-form";
        }

        if (userService.existsByUsername(user.getUsername(), user.getId())) {
            bindingResult.rejectValue("username", "error.user", "Этот логин уже используется!");
            model.addAttribute("allRoles", roleService.getAllRoles());
            return "user-form";
        }

        userService.saveUser(user, roleIds);
        return "redirect:/admin";
    }

    @GetMapping("/delete/{username}")
    public String deleteUser(@PathVariable String username) {
        userService.deleteUser(username);
        return "redirect:/admin";
    }
}
