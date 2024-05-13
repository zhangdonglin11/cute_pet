package com.example.cute_pet.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.cute_pet.domain.Comment;
import com.example.cute_pet.service.CommentService;
import com.example.cute_pet.mapper.CommentMapper;
import org.springframework.stereotype.Service;

/**
* @author 22212
* @description 针对表【comment】的数据库操作Service实现
* @createDate 2024-03-11 17:24:24
*/
@Service
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment>
    implements CommentService{

}




