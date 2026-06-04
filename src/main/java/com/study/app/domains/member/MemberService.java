package com.study.app.domains.member;

import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.study.app.domains.member.dto.InterestRegionDTO;
import com.study.app.domains.member.dto.InterestThemeDTO;
import com.study.app.domains.member.dto.MemberDTO;
import com.study.app.utils.JWTUtil;

@Service
public class MemberService {

    @Autowired
    private MemberDAO memberDAO;

    @Autowired
    private JWTUtil jwtUtil;

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    public int signup(MemberDTO memberDTO) {

        memberDTO.setPassword(bCryptPasswordEncoder.encode(memberDTO.getPassword()));

        if (memberDTO.getReside_area_code() == null) {
            memberDTO.setReside_area_code("0");
        }

        if (memberDTO.getReside_sigungu_code() == null) {
            memberDTO.setReside_sigungu_code("0");
        }

        if (memberDTO.getSocial_provider() == null) {
            memberDTO.setSocial_provider("LOCAL");
        }

        if (memberDTO.getProfile_image_url() == null) {
            memberDTO.setProfile_image_url("");
        }

        if (memberDTO.getTitle_id() == null) {
            memberDTO.setTitle_id(1L);
        }

        int result = memberDAO.insertMember(memberDTO);
        
        System.out.println("themes = " + memberDTO.getThemes());

        if (result > 0) {
            if (memberDTO.getThemes() != null) {
                Set<String> uniqueThemes = new HashSet<>(memberDTO.getThemes());

                for (String themeCode : uniqueThemes) {
                    System.out.println(
                            memberDTO.getMember_id() + " / " + themeCode
                        );
                    memberDAO.insertInterestTheme(
                        new InterestThemeDTO(memberDTO.getMember_id(), themeCode)
                    );
                }
            }

            if (memberDTO.getRegions() != null) {
                Set<String> uniqueRegions = new HashSet<>(memberDTO.getRegions());

                for (String regionCode : uniqueRegions) {
                    memberDAO.insertInterestRegion(
                        new InterestRegionDTO(memberDTO.getMember_id(), regionCode, null)
                    );
                }
            }
        }
        return result;
    }
}