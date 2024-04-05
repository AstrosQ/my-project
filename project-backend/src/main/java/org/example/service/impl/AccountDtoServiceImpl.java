package org.example.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.example.entity.dto.AccountDto;
import org.example.mapper.AccountDtoMapper;
import org.example.service.AccountDtoService;
import org.example.utils.Const;
import org.example.utils.FlowUtils;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
public class AccountDtoServiceImpl extends ServiceImpl<AccountDtoMapper, AccountDto> implements AccountDtoService {

    @Resource
    FlowUtils utils;

    @Resource
    AmqpTemplate amqpTemplate;

    @Resource
    StringRedisTemplate stringRedisTemplate;

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

    // 注册邮件，类型、地址、ip
    @Override
    public String registerEmailVerifyCode(String type, String email, String ip) {
        // 加锁排队
        synchronized (ip.intern()) {
        // 先验证是否在冷却期
        if (!this.verifyLimit(ip))
            return "请求频繁，请稍后再试";
        // 生成验证码
        Random random = new Random();
        int code = random.nextInt(900000) + 100000;
        // 存储丢经消息列里的信息
        Map<String, Object> data = Map.of("type", type, "email", email, "ip", ip);
        amqpTemplate.convertAndSend("mail", data);
        // 存进redis里进行校验
            stringRedisTemplate.opsForValue()
                .set(Const.VERIFY_EMAIL_LIMIT + email, String.valueOf(code), 3, TimeUnit.MINUTES);
        return null;
    }
    }

     private boolean verifyLimit(String ip){
        String key = Const.VERIFY_EMAIL_LIMIT + ip;
        return utils.limitOnceCheck(key, 60);
    }
}
