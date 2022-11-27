package com.example.application.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class DisplayNameService {

    Map<UserDetails, String> userToNameMap;

    public DisplayNameService() {
        userToNameMap = new HashMap<>();
    }

    public void setDisplayName(UserDetails userDetails, String displayName) {
        userToNameMap.put(userDetails, displayName);
    }

    public Optional<String> getDisplayName(UserDetails userDetails) {
        return Optional.ofNullable(userToNameMap.get(userDetails));
    }
}
