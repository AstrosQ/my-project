package org.example.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.example.entity.dto.AccountDto;
import org.example.entity.vo.request.ConfirmResetVO;
import org.example.entity.vo.request.EmailRegisterVO;
import org.example.entity.vo.request.EmailResetVO;
import org.example.mapper.AccountDtoMapper;
import org.example.service.AccountDtoService;
import org.example.utils.Const;
import org.example.utils.FlowUtils;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
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

    @Resource
    PasswordEncoder encoder;

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
            int code = random.nextInt(899999) + 100000;
            // 存储丢经消息列里的信息
            Map<String, Object> data = Map.of("type", type, "email", email, "code", code);
            amqpTemplate.convertAndSend("mail", data);
            // 存进redis里进行校验
                stringRedisTemplate.opsForValue()
                    .set(Const.VERIFY_EMAIL_DATA + email, String.valueOf(code), 3, TimeUnit.MINUTES);
            return null;
        }
    }

    @Override
    public String registerEmailAccount(EmailRegisterVO vo) {
        // 账户是否被注册，邮箱是否被占用
        String email = vo.getEmail();
        String username = vo.getUsername();
        String key = Const.VERIFY_EMAIL_DATA + email;
        // 取redis里的验证码
        String code = stringRedisTemplate.opsForValue().get(key);
        if (code == null) return "请先获取验证码！";
        if (!code.equals(vo.getCode())) return "验证码输入错误请重新输入！";
        if (this.exitsAccountByEmail(email)) return "此邮箱已注册！";
        if (this.exitsAccountByUsername(username)) return "此用户名已被其他用户注册！";
        String password = encoder.encode(vo.getPassword());
        AccountDto accountDto = new AccountDto(null, username, password, email, "user", new Date());
        if (this.save(accountDto)) {
            stringRedisTemplate.delete(key);
            return null;
        }else {
            return "内部错误，请联系管理员";
        }
    }

    @Override
    public String resetConfirm(ConfirmResetVO vo) {
        String email = vo.getEmail();
        String code = stringRedisTemplate.opsForValue().get(Const.VERIFY_EMAIL_DATA + email);
        if (code == null) return "请先获取验证码";
        if (!code.equals(vo.getCode())) return "验证码错误，请重新输入";
        return null;
    }

    @Override
    public String resetEmailAccountPassword(EmailResetVO vo) {
        String email = vo.getEmail();
        String verify = this.resetConfirm(new ConfirmResetVO(email, vo.getCode()));
        if (verify != null) return verify;
        String password = encoder.encode(vo.getPassword());
        boolean update = this.update().eq("email", email).set("password", password).update();
        if (update){
            stringRedisTemplate.delete(Const.VERIFY_EMAIL_DATA + email);
        }
        return null;
    }

    private boolean exitsAccountByEmail(String email){
        return baseMapper.exists(Wrappers.<AccountDto>query().eq("email", email));
    }

    private boolean exitsAccountByUsername(String username){
        return baseMapper.exists(Wrappers.<AccountDto>query().eq("username", username));
    }

    private boolean verifyLimit(String ip){
        String key = Const.VERIFY_EMAIL_LIMIT + ip;
        return utils.limitOnceCheck(key, 60);
    }
}
