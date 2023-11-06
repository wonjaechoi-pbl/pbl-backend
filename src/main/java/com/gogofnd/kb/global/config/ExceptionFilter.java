package com.gogofnd.kb.global.config;
import com.gogofnd.kb.global.dto.ErrorResponse;
import com.gogofnd.kb.global.error.model.ErrorCode;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.persistence.EntityNotFoundException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
@Component
public class ExceptionFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try{
            filterChain.doFilter(request,response);
        } catch (EntityNotFoundException ex){
            log.info("req :: " + request + " res :: " + response + " filterChain :: " + filterChain);
            log.error("exception exception handler filter");
            setErrorResponse(HttpStatus.FORBIDDEN,response,ex);
        }
    }

    public void setErrorResponse(HttpStatus status, HttpServletResponse response,Throwable ex){
        response.setStatus(status.value());
        response.setContentType("application/json");
        log.info("filter :: " + response);
        ErrorResponse errorResponse = new ErrorResponse(ErrorCode.HANDLE_ACCESS_DENIED);
        try{
            Gson gson = new Gson();
            String json = gson.toJson(errorResponse);
            //String json = errorResponse.convertToJson();
            System.out.println(json);
            response.getWriter().write(json);
        }catch (IOException e){
            e.printStackTrace();
        }
    }
}