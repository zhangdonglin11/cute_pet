package com.example.cute_pet.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.cute_pet.domain.ChatComment;
import com.example.cute_pet.service.ChatCommentService;
import com.example.cute_pet.mapper.ChatCommentMapper;
import org.springframework.stereotype.Service;

/**
* @author 22212
* @description 针对表【chat_comment】的数据库操作Service实现
* @createDate 2024-03-11 17:24:24
*/
@Service
public class ChatCommentServiceImpl extends ServiceImpl<ChatCommentMapper, ChatComment>
    implements ChatCommentService{

}




