package org.example.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.entity.dto.AccountDto;
import org.example.mapper.AccountDtoMapper;
import org.example.service.AccountDtoService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class AccountDtoServiceImpl extends ServiceImpl<AccountDtoMapper, AccountDto> implements AccountDtoService {


    // 查询自定义用户信息
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        AccountDto accountDto = this.findAccountDtoByNameOrEmail(username);  // 创建对象时就查询
        if (accountDto == null)  // 如果没查到就抛出异常
            throw new UsernameNotFoundException("用户名或密码错误！");
        return User  // 没问题就转化为User对象
                .withUsername(username)
                .password(accountDto.getPassword())
                .roles(accountDto.getRole())
                .build();
    }

    public AccountDto findAccountDtoByNameOrEmail(String text){
        return this.query()
                .eq("username",text).or()  // 如果username或者email等于我们要查的，就返回
                .eq("email",text)
                .one();
    }
}
