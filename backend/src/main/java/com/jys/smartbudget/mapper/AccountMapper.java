package com.jys.smartbudget.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import com.jys.smartbudget.dto.AccountDto;

@Mapper
public interface AccountMapper {
 
    List<AccountDto> getAccounts();
  
}
