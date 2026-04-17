package com.alshifa.mlms_al_shifa.service;

import com.alshifa.mlms_al_shifa.model.Role;
import com.alshifa.mlms_al_shifa.model.User;
import com.alshifa.mlms_al_shifa.repository.RoleRepository;
import com.alshifa.mlms_al_shifa.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository  userRepository;
    private final RoleRepository  roleRepository;
    private final PasswordEncoder passwordEncoder;

    /*
     * @Transactional keeps Hibernate session open so
     * EAGER roles collection is fully initialized before
     * the session closes. Prevents LazyInitializationException
     * that was silently dropping users from the table.
     */
    @Transactional(readOnly = true)
    public List<User> getAll() {
        return userRepository.findAllOrdered();
    }

    @Transactional(readOnly = true)
    public List<User> search(String query) {
        return userRepository.search(query);
    }

    @Transactional(readOnly = true)
    public User getById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "User not found: " + id));
    }

    @Transactional(readOnly = true)
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public long countAll() {
        return userRepository.count();
    }

    @Transactional
    public User createUser(
            String fullName,
            String username,
            String rawPassword,
            String email,
            String phone,
            String roleName) {

        if (userRepository.existsByUsername(username)) {
            throw new RuntimeException(
                    "Username already taken: " + username);
        }

        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Role not found: " + roleName));

        User user = User.builder()
                .fullName(fullName)
                .username(username)
                .password(passwordEncoder.encode(rawPassword))
                .email(email != null && !email.isBlank()
                        ? email : null)
                .phone(phone != null && !phone.isBlank()
                        ? phone : null)
                .enabled(true)
                .roles(Set.of(role))
                .build();

        return userRepository.save(user);
    }

    @Transactional
    public User updateUser(
            Long id,
            String fullName,
            String email,
            String phone,
            String roleName) {

        User user = getById(id);
        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Role not found: " + roleName));

        user.setFullName(fullName);
        user.setEmail(email != null && !email.isBlank()
                ? email : null);
        user.setPhone(phone != null && !phone.isBlank()
                ? phone : null);
        user.getRoles().clear();
        user.getRoles().add(role);

        return userRepository.save(user);
    }

    @Transactional
    public void resetPassword(Long id, String newRaw) {
        User user = getById(id);
        user.setPassword(passwordEncoder.encode(newRaw));
        userRepository.save(user);
    }

    @Transactional
    public void toggleEnabled(Long id) {
        User user = getById(id);
        user.setEnabled(!user.isEnabled());
        userRepository.save(user);
    }

    @Transactional
    public void delete(Long id) {
        userRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<User> getUsersByRole(String roleName) {
        return userRepository.findAll().stream()
                .filter(u -> u.getRoles().stream()
                        .anyMatch(r ->
                                r.getName().equals(roleName)))
                .toList();
    }
}