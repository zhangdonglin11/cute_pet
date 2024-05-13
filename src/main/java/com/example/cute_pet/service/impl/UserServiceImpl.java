package com.example.cute_pet.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.cute_pet.domain.User;
import com.example.cute_pet.mapper.UserMapper;
import com.example.cute_pet.service.UserService;
import org.springframework.stereotype.Service;

/**
* @author 22212
* @description 针对表【user】的数据库操作Service实现
* @createDate 2024-03-11 17:24:25
*/
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
    implements UserService {

}




