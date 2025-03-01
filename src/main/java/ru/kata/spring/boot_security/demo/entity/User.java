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
import java.util.Objects;
import java.util.stream.Collectors;

@Entity
@Table(name = "users")
public class User implements UserDetails {

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
    private boolean enabled = true;

    @Size(min = 1, message = "Логин не должен быть пустым")
    @Pattern(regexp = "^(|[A-Za-z0-9]+)$", message = "Логин должен содержать только буквы латинского алфавита")
    @Column(name = "username", unique = true)
    private String username;

    //@Size(min = 8, message = "Пароль должен состоять из 8 символов и больше")
    @Column(name = "password")
    private String password;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();


    public User() {
        this.enabled = true;
    }

    public User(String username, String password, String name, String lastName, Integer age, boolean enabled) {
        this.username = username;
        this.password = password;
        this.name = name;
        this.lastName = lastName;
        this.age = age;
        this.enabled = enabled;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles;
    }

    public String rolesToString() {
        return roles.stream()
                .map(Role::getAuthority)
                .collect(Collectors.joining(", "));
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
        return enabled;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public Integer getAge() {
        return age != null ? age : 0;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Set<Role> getRoles() {
        return roles;
    }

    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }

    @Override
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;
        User user = (User) o;
        return Objects.equals(id, user.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }


    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", name='" + name + '\'' +
                ", lastName='" + lastName + '\'' +
                ", age=" + age +
                ", enabled=" + enabled +
                ", roles=" + roles +
                '}';
    }
}
