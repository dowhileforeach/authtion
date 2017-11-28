package ru.dwfe.authtion.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class WebSecurityConfig extends WebSecurityConfigurerAdapter
{
    @Autowired
    private UserDetailsService userDetailsService;

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception
    {
        auth
                .userDetailsService(userDetailsService)         //проверка существования входящего user ID в базе
                .passwordEncoder(new BCryptPasswordEncoder());  //проверка raw пароля пользователя

        //Любой алгоритм кодирования(хеширования) не предназначен для обеспечения безопасности.
        //Поэтому на стороне бэкенда пароли в базе в виде BCrypt хеша это всего лишь попытка
        //защититься от дурака, который максимум может скопировать чужой пароль в открытом виде
        //либо его хеш и который не умеет/желает брутфорсить.
    }

    @Bean
    @Primary
    @Override
    protected AuthenticationManager authenticationManager() throws Exception
    {
        return super.authenticationManager();
    }
}
