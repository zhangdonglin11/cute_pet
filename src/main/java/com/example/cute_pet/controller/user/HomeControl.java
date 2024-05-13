package com.example.cute_pet.controller.user;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.cute_pet.controller.Common;
import com.example.cute_pet.domain.Comment;
import com.example.cute_pet.domain.Pet;
import com.example.cute_pet.domain.Picture;
import com.example.cute_pet.domain.Thumb;
import com.example.cute_pet.service.*;
import com.example.cute_pet.util.Result;
import com.example.cute_pet.util.TokenUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * 小程序首页相关接口
 */
@RestController
@RequestMapping("/api/user/home")
public class HomeControl {
    @Autowired
    private Common common;
    @Autowired
    private PetService petService;

    /**
     * 获取首页筛选框的数据
     */
    @GetMapping("/condition")
    public Result<HashMap<String, Object>> getCondition() {
        HashMap<String, Object> condition = new HashMap<>();
        String[] state = {"找朋友", "找对象", "找领养", "找代溜"};
        String[] sex = {"弟弟", "妹妹"};
        String[] age = {"一岁以内", "1-3岁", "3-7岁", "7岁以上"};
        condition.put("state", state);
        condition.put("sex", sex);
        condition.put("age", age);
        return new Result<>(condition, true, "请求成功");
    }


    /**
     * 首页筛选宠物
     */
    @PostMapping("/pet/show")
    public Result<HashMap<String, Object>> getPetShowList(@RequestBody HashMap<String, String> filtrate,
                                                          @RequestHeader("token") String token) {

        System.out.println(filtrate);
        QueryWrapper<Pet> petCardQueryWrapper = new QueryWrapper<>();
        if (filtrate.containsKey("petType") && filtrate.get("petType") != null) {
            petCardQueryWrapper.like("pet_type", filtrate.get("petType"));
        }
        if (filtrate.containsKey("address") && filtrate.get("address") != null) {
            petCardQueryWrapper.like("pet_address", filtrate.get("address"));
        }
        if (filtrate.containsKey("state") && filtrate.get("state") != null) {
            petCardQueryWrapper.like("pet_status", filtrate.get("state"));
        }
        if (filtrate.containsKey("sex") && filtrate.get("sex") != null) {
            // 执行相应的逻辑
            petCardQueryWrapper.like("pet_sex", filtrate.get("sex"));
        }
        if (filtrate.containsKey("age") && filtrate.get("age") != null) {
            // 执行相应的逻辑
            petCardQueryWrapper.like("pet_age", filtrate.get("age"));
        }
        petCardQueryWrapper.between("status", 1, 2);

        int current = Integer.parseInt(filtrate.get("current"));
        int size = Integer.parseInt(filtrate.get("size"));

        Page<Pet> page = new Page<>(current, size);
        Page<Pet> pageList = petService.page(page, petCardQueryWrapper);

        // 宠物数组
        ArrayList<Object> petArr = new ArrayList<>();
        for (Pet pet : pageList.getRecords()) {
            // 1.获取宠物图片数组
            ArrayList<String> picArr = common.getPicList("pet_id",pet.getId());
            // 2.宠物的获取评论
            HashMap<String, Object> petComment = common.getPetComment("pet_id",pet.getId());
            // 3.获取宠物的点赞状态
            HashMap<String, Object> petLike = common.getPetLike("pet_id",pet.getId(), token);
            HashMap<String, Object> petMap = new HashMap<>();
            petMap.put("pet",pet);
            petMap.put("pic",picArr);
            petMap.put("petComment",petComment);
            petMap.put("petLike",petLike);
            petArr.add(petMap);
        }

        HashMap<String, Object> petObj = new HashMap<>();
        petObj.put("petArr", petArr);  //宠物数组
        petObj.put("total", pageList.getTotal());
        petObj.put("size", pageList.getSize());
        petObj.put("current", pageList.getCurrent());
        petObj.put("pages", pageList.getPages());

        return new Result<>( 1, "请求成功",petObj);
    }
}
