package com.study.app.interceptors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import com.study.app.utils.JWTUtil;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class TokenValidator implements HandlerInterceptor {
	
	@Autowired
	private JWTUtil jwt;

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {
		
		if(request.getMethod().equalsIgnoreCase("OPTIONS")) {
			response.setStatus(HttpServletResponse.SC_OK);
			return true;
		}
		
		// 요청 경로 확인
	    String path = request.getRequestURI();
		
	    // 토큰 없이도 통과 가능한 '선택적 인증' 경로들
		boolean isOptionalPath = path.startsWith("/api/festivals/top") 
			|| path.startsWith("/api/festivals/closing-soon")
			|| path.startsWith("/api/festivals/random")
			|| path.equals("/api/festivals")
			|| path.startsWith("/api/festivals/sido");

		String authHeader = request.getHeader("Authorization");
		String token = null;

		if(authHeader != null && authHeader.startsWith("Bearer ")) {
			token = authHeader.substring(7);
		} else {
			token = request.getParameter("token");
		}

		// 토큰이 있는 경우 검증 시도
		if(token != null && !token.isEmpty() && !"null".equals(token)) {
			try {
				String id = jwt.getSubject(token);
				request.setAttribute("id", id);
				return true;
			} catch(Exception e) {
				// 토큰 검증 실패 시: 선택적 경로면 그냥 통과, 아니면 에러
				if (isOptionalPath) return true;
				e.printStackTrace();
			}
		}

		// 토큰이 없는 경우: 선택적 경로면 통과, 아니면 401
		if (isOptionalPath) {
			return true;
		}
		
		response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
		return false;

	}
}
