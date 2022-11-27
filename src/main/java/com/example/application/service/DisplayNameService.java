package com.example.application.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class DisplayNameService {

    Map<String, String> userIdToDisplayNameMap;

    public DisplayNameService() {
        userIdToDisplayNameMap = new HashMap<>();
    }

    public void setDisplayName(UserDetails userDetails, String displayName) {
        userIdToDisplayNameMap.put(userDetails.getUsername(), displayName);
    }

    public Optional<String> getDisplayName(UserDetails userDetails) {
        return Optional.ofNullable(userIdToDisplayNameMap.get(userDetails.getUsername()));
    }
}
