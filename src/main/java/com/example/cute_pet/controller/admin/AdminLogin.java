package com.example.cute_pet.controller.admin;

import com.example.cute_pet.domain.User;
import com.example.cute_pet.util.Result;
import com.example.cute_pet.util.TokenUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Objects;

@RestController
@RequestMapping("api/admin/login")
public class AdminLogin {
    @PostMapping("")
    public Result adminLogin(@RequestBody HashMap<String,String> fromData){
        System.out.println(fromData.toString());
        if (Objects.equals(fromData.get("username"), "admin") && Objects.equals(fromData.get("password"), "123456")){
            System.out.println(123);
            return new Result<>(1,"登录成功");
        }
        return new Result<>(0,"登录失败");
    }
}
