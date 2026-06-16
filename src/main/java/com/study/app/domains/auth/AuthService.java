package com.study.app.domains.auth;

import java.util.HashMap;
import java.util.Map;

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

import com.study.app.domains.auth.dto.LoginDTO;
import com.study.app.domains.member.dto.MemberDTO;
import com.study.app.domains.oauth.GoogleTokenResponse;
import com.study.app.domains.oauth.GoogleUserResponse;
import com.study.app.domains.oauth.KakaoTokenResponse;
import com.study.app.domains.oauth.KakaoUserResponse;
import com.study.app.domains.oauth.NaverTokenResponse;
import com.study.app.domains.oauth.NaverUserResponse;
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
    
    // 카카오 로그인
    @Value("${kakao.client-id}")
    private String kakaoClientId;

    @Value("${kakao.redirect-uri}")
    private String kakaoRedirectUri;

    // 네이버 로그인
    @Value("${naver.client-id}")
    private String naverClientId;

    @Value("${naver.client-secret}")
    private String naverClientSecret;

    @Value("${naver.redirect-uri}")
    private String naverRedirectUri;
    
    
    // 구글 로그인
    @Value("${google.client-id}")
    private String googleClientId;

    @Value("${google.client-secret}")
    private String googleClientSecret;

    @Value("${google.redirect-uri}")
    private String googleRedirectUri;
    
    
    public String login(LoginDTO loginDTO) {
        MemberDTO storedMember = authDAO.selectMemberById(loginDTO.getMember_id());
        
        if (storedMember != null) {
            // 탈퇴한 회원인지 체크
            if ("DELETED".equals(storedMember.getStatus())) {
                throw new RuntimeException("탈퇴 처리된 계정입니다. 고객센터에 문의하세요.");
            }

        	// Use BCryptPasswordEncoder.matches() to compare passwords
        	if (bCryptPasswordEncoder.matches(loginDTO.getPassword(), storedMember.getPassword())) {
        		// Passwords match, generate JWT token
        		return jwtUtil.createToken(storedMember.getMember_id());
        	}
        }
        return null; // Login failed
    }
    
    public Map<String, Object> kakaoLogin(String code) {

        Map<String, Object> result = new HashMap<>();

        KakaoTokenResponse tokenResponse = getKakaoAccessToken(code);
        

        if (tokenResponse == null || tokenResponse.getAccessToken() == null) {
            result.put("success", false);
            result.put("message", "카카오 access token 발급 실패");
            return result;
        }

        KakaoUserResponse userResponse =
            getKakaoUserInfo(tokenResponse.getAccessToken());

        if (userResponse == null) {
            result.put("success", false);
            result.put("message", "카카오 사용자 정보 조회 실패");
            return result;
        }

        String socialId = String.valueOf(userResponse.getId());
        String socialProvider = "KAKAO";

        MemberDTO member = authDAO.selectMemberBySocialIdAndProvider(
            socialId,
            socialProvider
        );

        // 기존 회원
        if (member != null) {
            String token = jwtUtil.createToken(member.getMember_id());

            result.put("success", true);
            result.put("isNewUser", false);
            result.put("token", token);
            result.put("member_id", member.getMember_id());
            result.put("nickname", member.getNickname());
            result.put("email", member.getEmail());
            result.put("social_provider", socialProvider);

            return result;
        }

        // 신규 회원
        String generatedMemberId = "kakao_" + socialId;
        
        System.out.println(generatedMemberId);

        result.put("member_id", generatedMemberId);
        result.put("social_provider", socialProvider);
        result.put("social_id", socialId);
        
        result.put("success", true);
        result.put("isNewUser", true);
        result.put("social_provider", socialProvider);
        result.put("social_id", socialId);
        result.put("email", "");
        result.put("nickname", "");
        result.put("name", "");
        result.put("profile_image_url", "");

        return result;
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

        HttpEntity<MultiValueMap<String, String>> request =
            new HttpEntity<>(params, headers);

        ResponseEntity<KakaoTokenResponse> response =
            restTemplate.postForEntity(
                tokenUrl,
                request,
                KakaoTokenResponse.class
            );

        return response.getBody();
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
    
    
    public Map<String, Object> naverLogin(
            String code,
            String state) {

        Map<String, Object> result = new HashMap<>();

        NaverTokenResponse tokenResponse =
            getNaverAccessToken(code, state);
        
        
        System.out.println("accessToken = "
        	    + tokenResponse.getAccessToken());

        if(tokenResponse == null) {
            result.put("success", false);
            return result;
        }

        NaverUserResponse userResponse =
            getNaverUserInfo(
                tokenResponse.getAccessToken()
            );

        String socialId =
            userResponse.getResponse().getId();

        String socialProvider = "NAVER";

        MemberDTO member =
            authDAO.selectMemberBySocialIdAndProvider(
                socialId,
                socialProvider
            );

        if(member != null) {

            String token =
                jwtUtil.createToken(
                    member.getMember_id()
                );

            result.put("success", true);
            result.put("isNewUser", false);
            result.put("token", token);
            
            result.put("member_id", member.getMember_id());
            result.put("nickname", member.getNickname());
            result.put("email", member.getEmail());

            result.put("social_provider", member.getSocial_provider());
            result.put("social_id", member.getSocial_id());
            


            return result;
        }

        result.put("success", true);
        result.put("isNewUser", true);

        result.put(
            "member_id",
            "naver_" + socialId
        );

        result.put(
            "social_provider",
            socialProvider
        );

        result.put(
            "social_id",
            socialId
        );

        result.put(
            "email",
            userResponse.getResponse().getEmail()
        );

        result.put(
            "nickname",
            userResponse.getResponse().getNickname()
        );

        result.put(
            "name",
            userResponse.getResponse().getName()
        );

        result.put(
            "profile_image_url",
            userResponse.getResponse().getProfileImage()
        );

        return result;
    }
    
    private NaverTokenResponse getNaverAccessToken(
            String code,
            String state) {

        String url =
            "https://nid.naver.com/oauth2.0/token" +
            "?grant_type=authorization_code" +
            "&client_id=" + naverClientId +
            "&client_secret=" + naverClientSecret +
            "&code=" + code +
            "&state=" + state;

        ResponseEntity<NaverTokenResponse> response =
            restTemplate.getForEntity(
                url,
                NaverTokenResponse.class
            );
        
        return response.getBody();
    }
    
    private NaverUserResponse getNaverUserInfo(
            String accessToken) {

        String url =
            "https://openapi.naver.com/v1/nid/me";

        HttpHeaders headers =
            new HttpHeaders();

        headers.setBearerAuth(accessToken);

        HttpEntity<?> request =
            new HttpEntity<>(headers);

        ResponseEntity<NaverUserResponse> response =
            restTemplate.exchange(
                url,
                HttpMethod.GET,
                request,
                NaverUserResponse.class
            );

        return response.getBody();
    }
    
    
    public Map<String, Object> googleLogin(String code) {

        Map<String, Object> result = new HashMap<>();

        GoogleTokenResponse tokenResponse =
            getGoogleAccessToken(code);
        


        if (tokenResponse == null || tokenResponse.getAccessToken() == null) {
            result.put("success", false);
            result.put("message", "구글 access token 발급 실패");
            return result;
        }

        System.out.println("google accessToken = " + tokenResponse.getAccessToken());

        GoogleUserResponse userResponse =
            getGoogleUserInfo(tokenResponse.getAccessToken());

        if (userResponse == null) {
            result.put("success", false);
            result.put("message", "구글 사용자 정보 조회 실패");
            return result;
        }

        String socialId = userResponse.getId();
        String socialProvider = "GOOGLE";

        MemberDTO member =
            authDAO.selectMemberBySocialIdAndProvider(
                socialId,
                socialProvider
            );

        if (member != null) {
            String token = jwtUtil.createToken(member.getMember_id());

            result.put("success", true);
            result.put("isNewUser", false);
            result.put("token", token);
            result.put("member_id", member.getMember_id());
            result.put("nickname", member.getNickname());
            result.put("email", member.getEmail());
            result.put("social_provider", socialProvider);

            return result;
        }

        result.put("success", true);
        result.put("isNewUser", true);
        result.put("member_id", "google_" + socialId);
        result.put("social_provider", socialProvider);
        result.put("social_id", socialId);
        result.put("email", userResponse.getEmail());
        result.put("nickname", userResponse.getName());
        result.put("name", userResponse.getName());
        result.put("profile_image_url", userResponse.getPicture());

        return result;
    }
    
    private GoogleTokenResponse getGoogleAccessToken(String code) {

        String tokenUrl = "https://oauth2.googleapis.com/token";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", googleClientId);
        params.add("client_secret", googleClientSecret);
        params.add("redirect_uri", googleRedirectUri);
        params.add("code", code);

        HttpEntity<MultiValueMap<String, String>> request =
            new HttpEntity<>(params, headers);

        ResponseEntity<GoogleTokenResponse> response =
            restTemplate.postForEntity(
                tokenUrl,
                request,
                GoogleTokenResponse.class
            );

        return response.getBody();
    }
    
    private GoogleUserResponse getGoogleUserInfo(String accessToken) {

        String url = "https://www.googleapis.com/oauth2/v2/userinfo";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        HttpEntity<?> request = new HttpEntity<>(headers);

        ResponseEntity<GoogleUserResponse> response =
            restTemplate.exchange(
                url,
                HttpMethod.GET,
                request,
                GoogleUserResponse.class
            );

        return response.getBody();
    }

}
