package com.knob.backend.consumer.utils;

import com.knob.backend.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;

public class JwtAuthentication {
    public static Integer getUserId(String token) {
        Integer userId = -1; // not exist
        try {
            Claims claims = JwtUtil.parseJWT(token);
            userId = Integer.parseInt(claims.getSubject());
        } catch (Exception e) {
            throw new JwtException("Invalid JWT token");
        }
        return userId;
    }
}



