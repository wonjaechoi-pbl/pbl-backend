package com.gogofnd.kb.global.domain.application;

import com.gogofnd.kb.domain.rider.entity.Rider;
import com.gogofnd.kb.domain.rider.repository.RiderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;


//스프링 시큐리티 설정할 때 유저 디테일서비스 implements 해야함
@RequiredArgsConstructor
@Service
public class CustomUserDetailService implements UserDetailsService{
    private final RiderRepository riderRepository;

    @Override
    public UserDetails loadUserByUsername(String riderId) throws UsernameNotFoundException {

        Rider rider = riderRepository.findById(Long.valueOf(riderId)).orElse(null);

        return rider;
    }


}
