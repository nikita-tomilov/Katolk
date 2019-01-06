package com.programmer74.katolk.server.config;

import com.programmer74.katolk.server.auth.CustomAuthProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

import javax.servlet.http.HttpServletResponse;

@Configuration
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

  @Autowired
  CustomAuthProvider customAuthProvider;

  @Override protected void configure(final AuthenticationManagerBuilder auth)
  throws Exception {
    auth.authenticationProvider(customAuthProvider);
  }

  @Override protected void configure(final HttpSecurity http)
  throws Exception {
    http.authorizeRequests()
        .antMatchers("/dummy")
        .permitAll()
        .anyRequest().authenticated()
        .and()
        .csrf().disable()
        .httpBasic()
        .and()
        .logout()
        .logoutUrl("/api/user/logout")
        .permitAll()
        .logoutSuccessHandler((httpServletRequest, httpServletResponse, authentication) -> {
          httpServletResponse.setStatus(HttpServletResponse.SC_OK);
        })
        .invalidateHttpSession(true)
        .deleteCookies("JSESSIONID");
  }
}
