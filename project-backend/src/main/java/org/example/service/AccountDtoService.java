package org.example.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.entity.dto.AccountDto;
import org.example.entity.vo.request.EmailRegisterVO;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface AccountDtoService extends IService<AccountDto>, UserDetailsService {
    AccountDto findAccountDtoByNameOrEmail(String text);
    String registerEmailVerifyCode(String type, String email, String ip);
    String registerEmailAccount(EmailRegisterVO vo);
}
