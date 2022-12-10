package com.example.application.security;

import com.example.application.model.ApplicationUser;
import com.example.application.repository.UserRepository;
import com.example.application.service.DisplayNameService;
import com.vaadin.flow.spring.security.VaadinWebSecurity;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.provisioning.UserDetailsManager;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.StreamSupport;

@EnableWebSecurity
@Configuration
public class SecurityConfiguration
        extends VaadinWebSecurity {

    private final UserRepository userRepository;
    private final DisplayNameService displayNameService;

    public SecurityConfiguration(UserRepository userRepository, DisplayNameService displayNameService) {
        this.userRepository = userRepository;
        this.displayNameService = displayNameService;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests().requestMatchers("/public/**").permitAll();

        super.configure(http);

        setLoginView(http, LoginView.class);
    }

    @Override
    public void configure(WebSecurity web) throws Exception {
        super.configure(web);
    }

    @Bean
    public UserDetailsManager userDetailsService() {
        List<UserDetails> users = new ArrayList<>();
        UserDetails rico =
                User.withUsername("vivar")
                        .password("{noop}Rico.33")
                        .roles("ADMIN", "USER")
                        .build();
        users.add(rico);

        Iterable<ApplicationUser> applicationUsers = userRepository.findAll();
        List<UserDetails> databaseUsers = StreamSupport.stream(applicationUsers.spliterator(), false)
                .map(applicationUser -> User.withUsername(applicationUser.getUserName())
                        .password("{noop}" + applicationUser.getPassword())
                        .roles("USER")
                        .build())
                .toList();

        users.addAll(databaseUsers);


        databaseUsers.forEach(userDetails -> {
            String nickname = userRepository.findByUserName(userDetails.getUsername()).getNickname();
            displayNameService.setDisplayName(userDetails, nickname);
        });

        return new InMemoryUserDetailsManager(users);
    }
}