package edu.uth.nurseborn.services;

import edu.uth.nurseborn.models.User;
import edu.uth.nurseborn.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.security.core.userdetails.User.UserBuilder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collections;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Người dùng không tồn tại: " + username));

        // Chuyển đổi User thành UserDetails
        UserBuilder builder = org.springframework.security.core.userdetails.User.withUsername(user.getUsername());
        builder.password(user.getPasswordHash());
        builder.authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole().name())));
        builder.accountExpired(false);
        builder.accountLocked(false);
        builder.credentialsExpired(false);
        builder.disabled(false);

        return builder.build();
    }
}