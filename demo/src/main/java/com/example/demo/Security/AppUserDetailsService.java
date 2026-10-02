package com.example.demo.Security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.Model.AuthSource;
import com.example.demo.Repository.AuthSourceRepository;

@Service
public class AppUserDetailsService implements UserDetailsService {

    private final AuthSourceRepository repository;

    public AppUserDetailsService(AuthSourceRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        AuthSource user = repository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new UsernameNotFoundException("No account for username: " + username));
        return new AppUserDetails(user);
    }
}
