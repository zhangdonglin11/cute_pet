package com.example.cute_pet.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.cute_pet.domain.Chat;
import com.example.cute_pet.service.ChatService;
import com.example.cute_pet.mapper.ChatMapper;
import org.springframework.stereotype.Service;

/**
* @author 22212
* @description 针对表【chat】的数据库操作Service实现
* @createDate 2024-03-11 17:24:24
*/
@Service
public class ChatServiceImpl extends ServiceImpl<ChatMapper, Chat>
    implements ChatService{

}




