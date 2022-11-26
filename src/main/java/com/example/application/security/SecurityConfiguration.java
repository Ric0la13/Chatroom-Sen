package com.example.application.security;

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

@EnableWebSecurity
@Configuration
public class SecurityConfiguration
        extends VaadinWebSecurity {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // Delegating the responsibility of general configurations
        // of http security to the super class. It's configuring
        // the followings: Vaadin's CSRF protection by ignoring
        // framework's internal requests, default request cache,
        // ignoring public views annotated with @AnonymousAllowed,
        // restricting access to other views/endpoints, and enabling
        // ViewAccessChecker authorization.
        // You can add any possible extra configurations of your own
        // here (the following is just an example):

        // http.rememberMe().alwaysRemember(false);

        // Configure your static resources with public access before calling
        // super.configure(HttpSecurity) as it adds final anyRequest matcher
        http.authorizeRequests().antMatchers("/public/**")
                .permitAll();

        super.configure(http);

        // This is important to register your login view to the
        // view access checker mechanism:
        setLoginView(http, LoginView.class);
    }

    @Override
    public void configure(WebSecurity web) throws Exception {
        // Customize your WebSecurity configuration.
        super.configure(web);
    }

    /**
     * Demo UserDetailsManager which only provides two hardcoded
     * in memory users and their roles.
     * NOTE: This shouldn't be used in real world applications.
     */
    @Bean
    public UserDetailsManager userDetailsService() {
        UserDetails rico =
                User.withUsername("vivar")
                        .password("{noop}Rico.33")
                        .roles("USER")
                        .build();
        UserDetails jan =
                User.withUsername("grafj")
                        .password("{noop}Jan.9")
                        .roles("USER")
                        .build();
        UserDetails ben =
                User.withUsername("ebneb")
                        .password("{noop}Ben.6")
                        .roles("USER")
                        .build();
        UserDetails jannik =
                User.withUsername("helmj")
                        .password("{noop}Jannik.11")
                        .roles("USER")
                        .build();
        UserDetails admin =
                User.withUsername("zeihm")
                        .password("{noop}wwi21A!")
                        .roles("ADMIN")
                        .build();
        return new InMemoryUserDetailsManager(rico, jan, ben, jannik, admin);
    }
}