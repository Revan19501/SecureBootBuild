package ru.kata.spring.boot_security.demo.entity;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.*;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
public class User implements UserDetails {

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();


    public void setAge(Integer age) {
        this.age = age;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Size(min = 1, message = "Имя не должно быть пустым")
    @Pattern(regexp = "^(|[A-Za-zА-Яа-яЁё]+)$", message = "Имя должно содержать только буквы")
    @Column(name = "name")
    private String name;

    @Size(min = 1, message = "Фамилия не должна быть пустой")
    @Pattern(regexp = "^(|[A-Za-zА-Яа-яЁё]+)$", message = "Фамилия должна содержать только буквы")
    @Column(name = "lastname")
    private String lastName;

    @Min(value = 13, message = "Сервис доступен только пользователям от 13 лет")
    @Max(value = 150, message = "Вам не может быть больше 150 лет:)")
    @Column(name = "age")
    private Integer age;

    @Column(name = "enabled", nullable = false)
    private boolean enabled;

    @Size(min = 1, message = "Логин не должен быть пустым")
    @Pattern(regexp = "^(|[A-Za-z0-9]+)$", message = "Логин должен содержать только буквы латинского альфавита")
    @Column(name = "username")
    private String username;

    @Size(min = 8, message = "Пароль должен состоять из 8 символов и больше")
    @Column(name = "password")
    private String password;

    public Integer getAge() {
        return age != null ? age : 0;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    public Set<Role> getRoles() {
        return roles;
    }

    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setUsername(String username) {
        this.username = username;
    }



}
