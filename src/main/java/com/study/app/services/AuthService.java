package com.study.app.services;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.study.app.dao.AuthDAO;
import com.study.app.dto.LoginDTO;
import com.study.app.dto.KakaoTokenResponse;
import com.study.app.dto.KakaoUserResponse;
import com.study.app.dto.MemberDTO;
import com.study.app.utils.JWTUtil;

@Service
public class AuthService {

    @Autowired
    private AuthDAO authDAO;
    
    @Autowired
    private JWTUtil jwtUtil;
    
    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${kakao.client-id}")
    private String kakaoClientId;

    @Value("${kakao.redirect-uri}")
    private String kakaoRedirectUri;

    @Value("${kakao.client-secret}")
    private String kakaoClientSecret; // Optional, but good practice if enabled
    
    
    public String login(LoginDTO loginDTO) {
        MemberDTO storedMember = authDAO.selectMemberById(loginDTO.getMember_id());
        
        if (storedMember != null) {
        	// Use BCryptPasswordEncoder.matches() to compare passwords
        	if (bCryptPasswordEncoder.matches(loginDTO.getPassword(), storedMember.getPassword())) {
        		// Passwords match, generate JWT token
        		return jwtUtil.createToken(storedMember.getMember_id());
        	}
        }
        return null; // Login failed
    }

    public String kakaoLogin(String code) {
        // 1. Get Access Token from Kakao
        KakaoTokenResponse tokenResponse = getKakaoAccessToken(code);
        if (tokenResponse == null) {
            return null; // Failed to get access token
        }

        // 2. Get User Information from Kakao
        KakaoUserResponse userResponse = getKakaoUserInfo(tokenResponse.getAccessToken());
        if (userResponse == null) {
            return null; // Failed to get user info
        }

        // 3. User Lookup/Registration
        String socialId = String.valueOf(userResponse.getId());
        String socialProvider = "KAKAO";
        
        MemberDTO member = authDAO.selectMemberBySocialIdAndProvider(socialId, socialProvider);

        if (member == null) {
            // User does not exist, register new user
            member = registerKakaoUser(userResponse);
            if (member == null) {
                return null; // Failed to register user
            }
        }

        // 4. Generate JWT
        return jwtUtil.createToken(member.getMember_id());
    }

    private KakaoTokenResponse getKakaoAccessToken(String code) {
        String tokenUrl = "https://kauth.kakao.com/oauth/token";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", kakaoClientId);
        params.add("redirect_uri", kakaoRedirectUri);
        params.add("code", code);
        // params.add("client_secret", kakaoClientSecret); // Uncomment if client_secret is required and enabled

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

        try {
            ResponseEntity<KakaoTokenResponse> response = restTemplate.postForEntity(tokenUrl, request, KakaoTokenResponse.class);
            if (response.getStatusCode() == HttpStatus.OK) {
                return response.getBody();
            }
        } catch (Exception e) {
            // Log exception
            e.printStackTrace();
        }
        return null;
    }

    private KakaoUserResponse getKakaoUserInfo(String accessToken) {
        String userInfoUrl = "https://kapi.kakao.com/v2/user/me";

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<String> request = new HttpEntity<>(headers);

        try {
            ResponseEntity<KakaoUserResponse> response = restTemplate.exchange(userInfoUrl, HttpMethod.GET, request, KakaoUserResponse.class);
            if (response.getStatusCode() == HttpStatus.OK) {
                return response.getBody();
            }
        } catch (Exception e) {
            // Log exception
            e.printStackTrace();
        }
        return null;
    }

    private MemberDTO registerKakaoUser(KakaoUserResponse userResponse) {
        try {
            MemberDTO newMember = new MemberDTO();
            // Using Kakao ID as part of member_id for uniqueness. You might want a more sophisticated strategy.
            newMember.setMember_id("kakao_" + userResponse.getId()); 
            // Social login users don't have a traditional password, set a dummy or null
            newMember.setPassword(bCryptPasswordEncoder.encode(UUID.randomUUID().toString())); // Set a random BCrypt encoded password
            newMember.setSocial_provider("KAKAO");
            newMember.setSocial_id(String.valueOf(userResponse.getId()));
            
            // Set other fields from Kakao user info
            if (userResponse.getKakaoAccount() != null) {
                if (userResponse.getKakaoAccount().getEmail() != null) {
                    newMember.setEmail(userResponse.getKakaoAccount().getEmail());
                }
                if (userResponse.getKakaoAccount().getProfile() != null) {
                    newMember.setNickname(userResponse.getKakaoAccount().getProfile().getNickname());
                    newMember.setProfile_image_url(userResponse.getKakaoAccount().getProfile().getProfileImageUrl());
                }
            }

            newMember.setExp_point(0L); // Default experience points
            newMember.setTitle_id(1L); // Default title
            newMember.setCreated_at(LocalDateTime.now());
            
            // Default values for non-essential fields
            newMember.setName("카카오사용자"); // Default name
            newMember.setPhone("N/A");
            newMember.setGender("N/A");
            newMember.setAddr_sido("N/A");
            newMember.setAddr_sigungu("N/A");
            newMember.setReside_area_code("N/A");
            newMember.setReside_sigungu_code("N/A");

            authDAO.insertMember(newMember);
            return newMember;
        } catch (Exception e) {
            // Log exception
            e.printStackTrace();
        }
        return null;
    }
}
