package com.example.cute_pet.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.cute_pet.domain.Pet;
import com.example.cute_pet.mapper.PetMapper;
import com.example.cute_pet.service.PetService;
import org.springframework.stereotype.Service;

/**
* @author 22212
* @description 针对表【pet】的数据库操作Service实现
* @createDate 2024-03-11 17:24:25
*/
@Service
public class PetServiceImpl extends ServiceImpl<PetMapper, Pet>
    implements PetService {

}




