package com.app.health.tracking.HealthTracking.security;

import com.app.health.tracking.HealthTracking.jwt.AccessLoggingFilter;
import com.app.health.tracking.HealthTracking.jwt.TokenValidationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.http.HttpServletResponse;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private TokenValidationFilter tokenValidationFilter;

    @Autowired
    private AccessLoggingFilter accessLoggingFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .csrf().disable()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .authorizeRequests()
                .antMatchers("/api/auth/login", "/api/auth/register").permitAll()
                .antMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                .antMatchers("/api/health/devices").hasRole("ADMIN")
                .antMatchers("/api/users/**").hasAnyRole("USER", "ADMIN")
                .antMatchers("/api/devices").hasAnyRole("USER", "ADMIN")
                .antMatchers("/api/devices/user").hasAnyRole("USER", "ADMIN")
                .antMatchers("/api/health/data/user").hasAnyRole("USER", "ADMIN")
                .antMatchers("/api/users/me").hasAnyRole("USER", "ADMIN")
                .antMatchers("/api/health/data/me").hasAnyRole("USER", "ADMIN")
                .antMatchers("/api/health/data/**").hasAnyRole("USER", "ADMIN") // Allow USER and ADMIN to update/delete health data
                .antMatchers("/api/health/**").hasAnyRole("USER", "ADMIN")
                .anyRequest().authenticated()
                .and()
                .addFilterBefore(accessLoggingFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(tokenValidationFilter, AccessLoggingFilter.class)
                .exceptionHandling()
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied: Insufficient permissions");
                });
    }
}
//.antMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").permitAll() // Allow Swagger endpoints