package com.example.cute_pet.controller.user;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.cute_pet.controller.Common;
import com.example.cute_pet.domain.PhoneCode;
import com.example.cute_pet.domain.User;
import com.example.cute_pet.service.PhoneCodeService;
import com.example.cute_pet.service.UserService;
import com.example.cute_pet.util.Result;
import com.example.cute_pet.util.SMSUtils;
import com.example.cute_pet.util.TokenUtils;
import com.example.cute_pet.util.ValidateCodeUtils;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;

/**
 * 小程序登录相关接口
 */


@RestController
@RequestMapping("/api/user")
public class LoginControl {
    @Autowired
    private UserService userService;
    @Autowired
    private PhoneCodeService phoneCodeService;
    @Autowired
    private Common common;

    /**
     * 登录- 手机号密码一键登录
     */
    @PostMapping("m_login")
    public Result login(@RequestBody HashMap<String,String> req) {
        // 1. 先排除手机验证码不能为空
        if (req.get("mobile").isEmpty()&&req.get("password").isEmpty()){
            return new Result<>(0,"手机号/密码不能为空");
        }else {
            // 1.2 登录密码判断
            QueryWrapper<User> qwUser = new QueryWrapper<>();
            qwUser.eq("phone",req.get("mobile")).eq("password",req.get("password"));
            User user = userService.getOne(qwUser);
            if (user != null && user.getPhone() != null && !user.getPhone().isEmpty()){
                HashMap<String, Object> profile = common.handleProfile(user);
                return new Result<>(1, "登录成功", profile);
            }else {
                return  new Result<>(0,"密码错误");
            }
        }
    }

    /**
     * 获取-短信验证码-登录
     */
    @GetMapping("m_login/code")
    public Result getCode(@RequestParam("mobile") String mobile, HttpSession session) throws Exception {
        if (Objects.equals(mobile, "13169197359") || Objects.equals(mobile, "15360785076")) {
            // 调用生成验证码的方法
            String code = String.valueOf(ValidateCodeUtils.generateValidateCode(4));

            QueryWrapper<PhoneCode> eqCode = new QueryWrapper<PhoneCode>().eq("phone",mobile);
            PhoneCode oneCode = phoneCodeService.getOne(eqCode);
            if(oneCode != null){
                oneCode.setCode(code);
                phoneCodeService.updateById(oneCode);
            }else {
                PhoneCode phoneCode = new PhoneCode();
                phoneCode.setPhone(mobile);
                phoneCode.setCode(code);
                phoneCodeService.save(phoneCode);
            }
            // 调用发送验证码的方法，通过阿里云短信服务发送验证码
            SMSUtils.sendMessage("宠物萌小程序", "SMS_464376212", mobile, code);
            return new Result<>(1, "验证码请求成功");
        } else {
            return new Result<>(0, "开发阶段只能获取指定号码的验证码");
        }
    }

    /**
     * 登录/注册-短信验证码
     */
    @PostMapping("m_login/code")
    public Result loginCode(@RequestBody HashMap<String, String> req) {
        // 1.获取保存的验证码
        QueryWrapper<PhoneCode> eqCode = new QueryWrapper<PhoneCode>()
                .eq("phone", req.get("mobile"))
                .eq("code",req.get("code"));
        PhoneCode userCode = phoneCodeService.getOne(eqCode);
        // 判断验证码
        if (userCode!=null) {
            // 1.1 删除code
            phoneCodeService.removeById(userCode);
            // 1.2 查询用户表
            QueryWrapper<User> eqUser = new QueryWrapper<User>().eq("phone", req.get("mobile"));
            User user = userService.getOne(eqUser, true);
            // 1.3判断用户是否已经注册 登录或者注册
            if (user != null && user.getPhone() != null && !user.getPhone().isEmpty()) {
                // 1.3.1 已注册返回
                HashMap<String, Object> profile = common.handleProfile(user);
                return new Result<>(1, "登录成功", profile);
            } else {
                // 1.3.2 没有该用户 新建用户
                User createUser = new User();
                createUser.setPhone(req.get("mobile"));
                createUser.setPassword("123456");
                createUser.setRole("USER");
                createUser.setStatus(0);
                // 1.3.3 在用户表创建新的用户信息
                boolean saved = userService.save(createUser);
                if (saved){
                    // 1.3.4 创建用户后修改用户昵称
                    createUser.setNickname("铲屎官"+createUser.getId()+1124);
                    userService.updateById(createUser);
                    // 1.3.1 处理返回的个人信息
                    HashMap<String, Object> profile = common.handleProfile(createUser);
                    return new Result<>(1, "登录成功", profile);
                }
                return new Result<>(0, "注册失败");
            }
        }else {
            // 2.session手机号和验证码不对
            return new Result<>(0, "验证码错误");
        }

    }
}
