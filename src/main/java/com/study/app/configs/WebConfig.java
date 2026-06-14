package com.study.app.configs;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import com.study.app.interceptors.TokenValidator;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private TokenValidator tokenValidator;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
    	// 로그인이 '필수'인 축제/모임 특정 API들만 저격해서 인터셉터 등록
        registry.addInterceptor(tokenValidator)
                .addPathPatterns(
                        "/api/festivals/likeList",   // 나의 찜 목록 조회
                        "/api/festivals/likeToggle", // 찜하기 토글
                        "/api/festivals/sync"        // 축제 데이터 동기화
                );

        // 2. 기존 전역 설정 (여기서는 축제 전체/**를 제외하여 비회원 자유 권한을 유지)
        registry.addInterceptor(tokenValidator)
                .addPathPatterns("/activities/**")
                .addPathPatterns("/ai/**")
                .addPathPatterns("/api/**")
                .addPathPatterns("/member/profile/**")
                .excludePathPatterns( 
                        "/api/festivals/**", // 1번에서 저격한 주소 외의 모든 축제 API는 토큰 없이 패스
                        "/api/gathering/**"  
                );   
    }
}
