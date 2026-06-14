package com.study.app.domains.auth;

import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.study.app.domains.auth.dto.EmailVerificationDTO;

@Service
public class EmailService {
	
	@Autowired
    private JavaMailSender mailSender;
	
	@Autowired
    private EmailDAO emailDAO;


    @Transactional
    public void sendVerificationCode(String email) {

        String code = createCode();

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MINUTE, 5);

        EmailVerificationDTO dto = new EmailVerificationDTO();
        dto.setEmail(email);
        dto.setCode(code);
        dto.setExpires_at(LocalDateTime.now().plusMinutes(5));

        EmailVerificationDTO old = emailDAO.selectByEmail(email);

        if (old == null) {
            emailDAO.insertCode(dto);
        } else {
            emailDAO.updateCode(dto);
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("[축제로] 이메일 인증번호");
        message.setText(
            "안녕하세요. 축제로입니다.\n\n" +
            "이메일 인증번호는 [" + code + "] 입니다.\n\n" +
            "인증번호는 5분 동안 유효합니다."
        );

        mailSender.send(message);
    }

    @Transactional
    public boolean verifyCode(String email, String code) {

        EmailVerificationDTO dto = emailDAO.selectByEmail(email);

        if (dto == null) {
            return false;
        }

        if (dto.getExpires_at().isBefore(LocalDateTime.now())) {
            return false;
        }

        if (!dto.getCode().equals(code)) {
            return false;
        }

        emailDAO.verifyEmail(email);
        return true;
    }

    public boolean isEmailVerified(String email) {
        return emailDAO.isEmailVerified(email);
    }

    private String createCode() {
        Random random = new Random();
        int number = random.nextInt(900000) + 100000;
        return String.valueOf(number);
    }

}
