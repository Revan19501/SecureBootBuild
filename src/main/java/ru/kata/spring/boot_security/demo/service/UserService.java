package ru.kata.spring.boot_security.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.kata.spring.boot_security.demo.entity.Role;
import ru.kata.spring.boot_security.demo.entity.User;
import ru.kata.spring.boot_security.demo.repositories.RoleRepository;
import ru.kata.spring.boot_security.demo.repositories.UserRepository;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }
    @Transactional
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
    @Transactional
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }
    @Transactional
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    @Transactional
    public boolean existsByUsername(String username, Long id) {
        Optional<User> existingUser = userRepository.findByUsername(username);
        return existingUser.isPresent() && !existingUser.get().getId().equals(id);
    }

    @Transactional
    public void saveUser(User user, List<Long> roleIds) {
        User userToSave = user.getId() != null ? findById(user.getId()).orElse(new User()) : new User();

        userToSave.setName(user.getName());
        userToSave.setLastName(user.getLastName());
        userToSave.setAge(user.getAge());
        userToSave.setEnabled(user.isEnabled());
        userToSave.setUsername(user.getUsername());

        if (user.getId() != null) {
            User existingUser = findById(user.getId()).orElse(null);
            if (existingUser != null) {
                if (user.getPassword() == null || user.getPassword().isEmpty()) {
                    userToSave.setPassword(existingUser.getPassword());
                } else {
                    userToSave.setPassword(passwordEncoder.encode(user.getPassword()));
                }
            } else {
                userToSave.setPassword(passwordEncoder.encode(user.getPassword()));
            }
        } else {
            userToSave.setPassword(passwordEncoder.encode(user.getPassword()));
        }

        if (roleIds != null && !roleIds.isEmpty()) {
            Set<Role> roles = roleIds.stream()
                    .map(roleRepository::findById)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(Collectors.toSet());
            userToSave.setRoles(roles);
        }

        userRepository.save(userToSave);
    }
    @Transactional
    public void changePassword(String username, String newPassword) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Transactional
    public void deleteUser(String username) {
        userRepository.findByUsername(username).ifPresent(userRepository::delete);
    }

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return user;
    }
}
