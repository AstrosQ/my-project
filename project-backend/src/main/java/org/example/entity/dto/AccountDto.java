package org.example.entity.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.example.entity.BaseData;

import java.util.Date;

@Data
@TableName("db_account")
@AllArgsConstructor
public class AccountDto implements BaseData {
    @TableId(type = IdType.AUTO)  // 主键自动递增
    Integer id;
    String username;
    String password;
    String email;
    String role;
    Date registerTime;
}
