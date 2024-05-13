package com.example.cute_pet.controller.user;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.cute_pet.controller.Common;
import com.example.cute_pet.domain.Pet;
import com.example.cute_pet.domain.Picture;
import com.example.cute_pet.domain.Thumb;
import com.example.cute_pet.domain.User;
import com.example.cute_pet.service.CommentService;
import com.example.cute_pet.service.PetService;
import com.example.cute_pet.service.ThumbService;
import com.example.cute_pet.service.UserService;
import com.example.cute_pet.util.ImageUtil;
import com.example.cute_pet.util.NicknameValidator;
import com.example.cute_pet.util.Result;
import com.example.cute_pet.util.TokenUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Pattern;

/**
 * 小程序个人消息页面相关接口
 */
@RestController
@RequestMapping("api/user/mine")
public class MineControl {
    @Autowired
    private Common common;
    @Autowired
    private PetService petService;
    @Autowired
    private ThumbService thumbService;
    @Autowired
    private UserService userService;


    /**
     * 获取用户个人信息
     * Params{uid}
     */
    @GetMapping("profile")
    public Result getUserProfile(@RequestParam("uid") int uid){
        System.out.println(uid);
        User user = userService.getById(uid);
        if (user!=null){
            HashMap<String, Object> profile = common.handleProfile(user);
            return new Result<>(1, "请求成功", profile);
        }else {
            return new Result<>(0, "请求失败");
        }
    }


    /**
     * 获取我的宠物列表
     * Params{uid}
     */
    @GetMapping("pet")
    public Result findMyPetByUid(@RequestParam("uid") int uid, @RequestHeader("token") String token) {
        // 1.按照用户id查询都有宠物消息
        QueryWrapper<Pet> eqPet = new QueryWrapper<Pet>().eq("user_id", uid).ne("status", 0);
        List<Pet> petList = petService.list(eqPet);
        // 2.遍历设置每个宠物的所有数据
        ArrayList<Object> petArr = new ArrayList<>();
        petList.forEach(item -> {
            // 1.获取宠物图片数组
            ArrayList<String> picArr = common.getPicList("pet_id", item.getId());
            // 2.宠物的获取评论
            HashMap<String, Object> petComment = common.getPetComment("pet_id", item.getId());
            // 3.获取宠物的点赞状态
            HashMap<String, Object> petLike = common.getPetLike("pet_id", item.getId(), token);

            HashMap<String, Object> newPet = new HashMap<>();
            // 宠物基本消息
            newPet.put("pet", item);
            // 宠物图片数组
            newPet.put("pic", picArr);
            // 宠物所有评论
            // newPet.put("petComment", petComment);
            // 宠物点赞状态
            newPet.put("petLike", petLike);
            petArr.add(newPet);
        });
        return new Result<>(1, "请求成功", petArr);
    }

    /**
     * 获取我喜欢的宠物列表
     * Params{uid}
     */
    @GetMapping("like")
    public Result findLikePetByUid(@RequestParam("uid") int uid, @RequestHeader("token") String token) {
        // 查询喜欢列表数据库的宠物id
        QueryWrapper<Thumb> eqUid = new QueryWrapper<Thumb>().eq("user_id", uid).isNotNull("pet_id");
        List<Thumb> likePetList = thumbService.list(eqUid);
        ArrayList<Object> petArr = new ArrayList<>();
        // 创建数组集合，保存所有宠物信息
        if (!likePetList.isEmpty()) {
            ArrayList<Integer> likePetIdArr = new ArrayList<>();
            likePetList.forEach(item -> {
                likePetIdArr.add(item.getPetId());
            });
            // 通过查询到的数组遍历id查询喜欢的宠物，列表查询
            List<Pet> petList = petService.listByIds(likePetIdArr);
            petList.forEach(item -> {
                // 1.获取宠物图片数组
                ArrayList<String> picArr = common.getPicList("pet_id", item.getId());
                // 2.宠物的获取评论
                HashMap<String, Object> petComment = common.getPetComment("pet_id", item.getId());
                // 3.获取宠物的点赞状态
                HashMap<String, Object> petLike = common.getPetLike("pet_id", item.getId(), token);

                HashMap<String, Object> newPet = new HashMap<>();
                // 宠物基本消息
                newPet.put("pet", item);
                // 宠物图片数组
                newPet.put("pic", picArr);
                // 宠物所有评论
                // newPet.put("petComment", petComment);
                // 宠物点赞状态
                newPet.put("petLike", petLike);
                petArr.add(newPet);
            });
            return new Result<>(1, "请求成功", petArr);
        } else {
            return new Result<>(1, "没有喜欢的宠物");
        }
    }

    /**
     * 修改用户的个人信息
     * Params{name,sex,password}
     */
    @PostMapping("profile")
    public Result setProfile(@RequestBody HashMap<String, Object> formData,
                             @RequestHeader("token") String token) {
        Integer userId = TokenUtils.getDecodedJWT(token).getClaim("id").asInt();
        User user = new User();
        user.setId(userId);
        // 设置用户名字 要求用户昵称中文6个字，英文最多12个
        if (formData.containsKey("name") && formData.get("name") != null) {
            if (NicknameValidator.validateNickname(formData.get("name").toString())) {
                user.setNickname(formData.get("name").toString());
            } else {
                return new Result<>(0, NicknameValidator.validateNicknameResult(formData.get("name").toString()));
            }
        }
        // 设置性别
        if (formData.containsKey("sex") && formData.get("sex") != null) {
            if (Objects.equals(formData.get("sex"), "男") || Objects.equals(formData.get("sex"), "女")) {
                user.setSex(formData.get("sex").toString());
            } else {
                return new Result<>(0, "用户性别错误");
            }
        }
        // 使用正则表达式匹配密码规则 字母+数字 6-16
        String pattern = "^(?![0-9]+$)(?![a-zA-Z]+$)[0-9A-Za-z]{6,16}$";
        if (formData.containsKey("password") && formData.get("password") != null) {
            if (Pattern.matches(pattern, formData.get("password").toString())) {
                user.setPassword(formData.get("password").toString());
            } else {
                return new Result<>(0, "密码格式错误");
            }
        }
        user.setStatus(1);
        userService.updateById(user);
        User newUser = userService.getById(userId);
        // 处理用户数据返回前端
        HashMap<String, Object> profile = common.handleProfile(newUser);
        return new Result<>(1, "修改成功", profile);
    }


    /**
     * 修改用户的头像图片
     * Params{file}
     */
    @PostMapping("profile/pic")
    public Result setProfilePic(@RequestBody MultipartFile mfile,
                                @RequestHeader("token") String token) {
        int userId = TokenUtils.getDecodedJWT(token).getClaim("id").asInt();
        User user = userService.getById(userId);
        // 保存图片到本地
        if (!mfile.isEmpty()) {
            // 保存文件操作
            // 获取文件后缀
            String suffixName = ImageUtil.getImagePath(mfile);
            // 生成新文件名称
            String newFileName = ImageUtil.getNewFileName(suffixName);
            // 保存文件 生成保存文件的地址
            File file = new File(ImageUtil.getNewImagePath(newFileName, "image/"));
            // 保存成功
            boolean state = ImageUtil.saveImage(mfile, file);
            // 保存位置
            if (state) {
                // 删除旧的图片
                String oldPath = user.getPic();
                common.deleteFileImg(oldPath);
                // 修改图片地址
                user.setPic("http://localhost:8088/image/" + newFileName);
                user.setStatus(2);
                boolean saved = userService.updateById(user);
                if (!saved) {
                    // 保存图片地址到数据库 失败就删除图片文件
                    if (file.exists()) {
                        FileSystemUtils.deleteRecursively(file);
                    } else {
                        System.out.println("目标文件不存在");
                    }
                }
                // 处理用户数据返回前端
                HashMap<String, Object> profile = common.handleProfile(user);
                return new Result<>(1, "保存图片成功", profile);
            }
        }
        return new Result<>(1, "保存图片失败");
    }
}
