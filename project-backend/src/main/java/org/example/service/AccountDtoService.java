package org.example.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.entity.dto.AccountDto;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface AccountDtoService extends IService<AccountDto>, UserDetailsService {
    AccountDto findAccountDtoByNameOrEmail(String text);
}
