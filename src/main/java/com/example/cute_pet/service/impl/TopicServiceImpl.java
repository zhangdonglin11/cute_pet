package com.example.cute_pet.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.cute_pet.domain.Topic;
import com.example.cute_pet.mapper.TopicMapper;
import com.example.cute_pet.service.TopicService;
import org.springframework.stereotype.Service;

/**
* @author 22212
* @description 针对表【topic】的数据库操作Service实现
* @createDate 2024-03-11 17:24:25
*/
@Service
public class TopicServiceImpl extends ServiceImpl<TopicMapper, Topic>
    implements TopicService {

}




