// package com.example.cute_pet.controller;
//
// import com.auth0.jwt.interfaces.DecodedJWT;
// import com.baomidou.mybatisplus.core.conditions.Wrapper;
// import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
// import com.baomidou.mybatisplus.core.toolkit.StringUtils;
// import com.example.cute_pet.domain.PhoneCode;
// import com.example.cute_pet.domain.User;
// import com.example.cute_pet.service.PhoneCodeService;
// import com.example.cute_pet.service.UserService;
// import com.example.cute_pet.util.ImageUtil;
// import com.example.cute_pet.util.TokenUtils;
// import jakarta.servlet.http.HttpServletRequest;
// import jakarta.servlet.http.HttpSession;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.util.FileSystemUtils;
// import org.springframework.util.ResourceUtils;
// import org.springframework.web.bind.annotation.*;
// import org.springframework.web.multipart.MultipartFile;
//
// import java.io.File;
// import java.io.FileNotFoundException;
// import java.io.IOException;
// import java.util.*;
//
// import static net.sf.jsqlparser.util.validation.metadata.NamedObject.schema;
// import static net.sf.jsqlparser.util.validation.metadata.NamedObject.user;
//
// // @Api( tags = {"用户模块接口"})
// @RestController
// @RequestMapping("user")
// public class UserController {
//     @Autowired
//     private UserService userService;
//     @Autowired
//     private PhoneCodeService phoneCodeService;
//
//
//     /**
//      * 登录接口 POST
//      *
//      * @ {phone,password}
//      */
//     @PostMapping(value = "login")
//     public Map<String, Object> login(@RequestBody User user) {
//         Map<String, Object> map = new HashMap<>();
//         if (StringUtils.isEmpty(user.getPhone()) || StringUtils.isEmpty(user.getPassword())) {
//             map.put("code", 0);
//             map.put("result", "");
//             map.put("msg", "手机号和密码不能为空！");
//             return map;
//         }
//         QueryWrapper<User> queryWrapper = new QueryWrapper<>();
//         queryWrapper.eq("phone", user.getPhone()).eq("password", user.getPassword());
//         User userDb = userService.getOne(queryWrapper);
//         if (userDb != null) {
//             String token = TokenUtils.sign(userDb);
//             map.put("code", 1);
//             map.put("msg", "登录成功");
//             map.put("result",  getNewUser(token, userDb));
//         } else {
//             map.put("code", 2);
//             map.put("msg", "邮箱或密码错误！");
//             map.put("result", "");
//         }
//         return map;
//     }
//
//     /**
//      * 注册接口 POST
//      *
//      * @ {phone,password,yzm}
//      */
//     @PostMapping(value = "register")
//     public Map<String, Object> register(@RequestBody Map<String, String> userMap) {
//         Map<String, Object> map = new HashMap<String, Object>();
//         if (StringUtils.isEmpty(userMap.get("yzm"))) {
//             map.put("code", 0);
//             map.put("msg", "验证码不能为空！");
//             return map;
//         }
//         if (StringUtils.isEmpty(userMap.get("phone")) || StringUtils.isEmpty(userMap.get("password"))) {
//             map.put("code", 0);
//             map.put("msg", "手机号和密码不能为空！");
//             return map;
//         }
//         // 查询验证码
//         Map<String, Object> codemap = new HashMap<String, Object>();
//         codemap.put("phone", userMap.get("phone"));
//         codemap.put("code", userMap.get("yzm"));
//         List<PhoneCode> listCode = phoneCodeService.listByMap(codemap);
//         if (listCode.isEmpty()) {
//             map.put("code", 0);
//             map.put("msg", "验证码错误！");
//             return map;
//         }
//         // 查询用户是否已注册
//         Map<String, Object> columnMap = new HashMap<String, Object>();
//         columnMap.put("phone", userMap.get("phone"));
//         List<User> users = userService.listByMap(columnMap);
//         if (!users.isEmpty()) {
//             map.put("code", 0);
//             map.put("msg", "用户已注册,去登录吧！");
//             return map;
//         }
//         // 注册
//         User user = new User();
//         user.setPhone(userMap.get("phone"));
//         user.setPassword(userMap.get("password"));
//         user.setRole("USER");
//         user.setStatus(0);
//         boolean save = userService.save(user);
//         if (save) {
//             user.setNickname("铲屎官" + (user.getId() + 1124));
//             boolean res = userService.updateById(user);
//             map.put("code", 1);
//             map.put("msg", "注册成功！去登录吧。");
//             return map;
//         }
//         map.put("code", 0);
//         map.put("msg", "注册失败！");
//         return map;
//     }
//
//     /**
//      * 修改密码接口 POST
//      *
//      * @ {phone,password,yzm}
//      */
//     // ("根据手机号修改密码")
//     @PostMapping(value = "retrieve")
//     public Map<String, Object> retrieve(@RequestBody Map<String, String> userMap) {
//         Map<String, Object> map = new HashMap<String, Object>();
//         if (StringUtils.isEmpty(userMap.get("yzm"))) {
//             map.put("code", 0);
//             map.put("msg", "验证码不能为空！");
//             return map;
//         }
//         if (StringUtils.isEmpty(userMap.get("phone")) || StringUtils.isEmpty(userMap.get("password"))) {
//             map.put("code", 0);
//             map.put("msg", "手机号和密码不能为空！");
//             return map;
//         }
//         // 查询验证码
//         Map<String, Object> codemap = new HashMap<String, Object>();
//         codemap.put("phone", userMap.get("phone"));
//         codemap.put("code", userMap.get("yzm"));
//         List<PhoneCode> listCode = phoneCodeService.listByMap(codemap);
//         if (listCode.isEmpty()) {
//             map.put("code", 0);
//             map.put("msg", "验证码错误！");
//             return map;
//         }
//         // 查询用户是否已注册
//         QueryWrapper<User> eqphone = new QueryWrapper<User>().eq("phone", userMap.get("phone"));
//         List<User> userList = userService.list(eqphone);
//         User user = new User();
//         System.out.println(userList);
//         // 结果为空，未注册
//         if (userList.isEmpty()) {
//             // 注册
//             user.setPhone(userMap.get("phone"));
//             user.setPassword(userMap.get("password"));
//             user.setRole("USER");
//             user.setStatus(0);
//             boolean save = userService.save(user);
//             if (!save) {
//                 map.put("code", 0);
//                 map.put("msg", "注册失败！");
//                 return map;
//             }
//             user.setNickname("铲屎官" + (user.getId() + 1124));
//             boolean res = userService.updateById(user);
//             map.put("code", 1);
//             map.put("msg", "注册成功，去登录吧！");
//             return map;
//         }
//         //  修改密码啊
//
//         user = userList.get(0);
//         user.setPassword(userMap.get("password"));
//         boolean res = userService.updateById(user);
//         System.out.println(res);
//         if (res) {
//             map.put("code", 1);
//             map.put("result", "");
//             map.put("msg", "密码修改成功,去登录吧！");
//
//             return map;
//         }
//         map.put("code", 0);
//         map.put("result", "");
//         map.put("msg", "密码修改失败！");
//         return map;
//     }
//
//     /**
//      * 验证码接口 POST
//      *
//      * @ {phone}
//      */
//     @PostMapping(value = "getcode")
//     public Map<String, Object> getVerificationCode(@RequestBody PhoneCode phoneCode) {
//         Map<String, Object> map = new HashMap<String, Object>();
//         Map<String, Object> data = new HashMap<String, Object>();
//         int min = 1000;
//         int max = 9999;
//
//         Random random = new Random();
//         String randomNumber = String.valueOf(random.nextInt(max - min + 1) + min);
//         phoneCode.setCode(randomNumber);
//
//         Map<String, Object> columnMap = new HashMap<String, Object>();
//         columnMap.put("phone", phoneCode.getPhone());
//         List<PhoneCode> listPhone = phoneCodeService.listByMap(columnMap);
//         // 有则修改，没有则添加
//         if (!listPhone.isEmpty()) {
//             phoneCodeService.updateById(phoneCode);
//         } else {
//             phoneCodeService.save(phoneCode);
//         }
//
//         data.put("yzm", randomNumber);
//         data.put("massage", "【宠物萌】您的验证码是" + randomNumber + ";请勿泄露给他人。");
//
//         map.put("code", 1);
//         map.put("result", data);
//         map.put("msg", "生成验证码！");
//
//         return map;
//     }
//
//
//     /**
//      * 修改用户昵称/头像
//      * param {nickname，prc,token}
//      */
//     @PostMapping("/upload")
//     public Map<String, Object> handleImageUpload(@RequestBody() MultipartFile mfile,
//                                                  @RequestParam("nickname") String nickname,
//                                                  @RequestHeader("token") String token) throws FileNotFoundException {
//         Map<String, Object> map = new HashMap<String, Object>();
//         map.put("code", 0);
//         map.put("msg", "操作失败");
//         // 获取用户id
//         int id = TokenUtils.getDecodedJWT(token).getClaim("id").asInt();
//
//         // 修改用户头像 处理接收到的图片和参数
//         if (!mfile.isEmpty()) {
//             // 1.获取用户数据库当前的头像地址
//             String oldImagePath = "";
//             String oldPrc = userService.getById(id).getPic();
//             if (!StringUtils.isEmpty(oldPrc)) {
//                 String oldFileName = oldPrc.substring(oldPrc.indexOf("image/") + 6);
//                 oldImagePath = ImageUtil.getNewImagePath(oldFileName, "image/");
//             }
//             System.out.println("旧的头像地址" + oldImagePath);
//
//             // 保存文件操作
//             // 获取文件后缀
//             String suffixName = ImageUtil.getImagePath(mfile);
//             // 生成新文件名称
//             String newFileName = ImageUtil.getNewFileName(suffixName);
//             // 保存文件 生成保存文件的地址
//             File file = new File(ImageUtil.getNewImagePath(newFileName, "image/"));
//             // 保存成功
//             boolean state = ImageUtil.saveImage(mfile, file);
//             // 保存位置
//             if (state) {
//                 // 修改头像地址
//                 User user = new User();
//                 user.setId(id);
//                 user.setPic("http://localhost:8088/image/" + newFileName);
//                 boolean state1 = userService.updateById(user);
//                 if (state1) {
//                     // 删除图片 判断空不能删除
//                     if (!oldImagePath.isEmpty()) {
//                         FileSystemUtils.deleteRecursively(new File(oldImagePath));
//                     }
//                     map.put("code", 1);
//                     map.put("msg", "修改成功");
//                     System.out.println("修改了图片");
//                 }
//             }
//         }
//         // 修改用户昵称
//         if (!StringUtils.isEmpty(nickname)) {
//             User user = new User();
//             user.setId(id);
//             user.setNickname(nickname);
//             userService.updateById(user);
//             map.put("code", 1);
//             map.put("msg", "修改成功");
//             System.out.println("修改了昵称");
//         }
//         User newUser = userService.getById(id);
//         // 传token、新的user
//         map.put("result",  getNewUser(token, newUser));
//         return map;
//     }
//
//     // 刷新个人信息接口
//     @GetMapping("User/get")
//     public Map<String,Object> getPersonal (@RequestHeader("token") String token) {
//         Map<String, Object> map = new HashMap<>();
//         User user = userService.getById(TokenUtils.getDecodedJWT(token).getClaim("id").asInt());
//         if (user == null){
//             map.put("code",0);
//             map.put("msg","");
//             map.put("result", "");
//             return map;
//         }
//         map.put("code",1);
//         map.put("msg","请求成功");
//         map.put("result", getNewUser(token,user));
//         return map;
//     }
//
//     // 返回个人信息的复用方法 登录和刷新个人信息
//     private   Map<String, Object> getNewUser(String token, User newUser) {
//         Map<String, Object> data = new HashMap<>();
//         data.put("id", newUser.getId());
//         data.put("nickname", newUser.getNickname());
//         data.put("phone", newUser.getPhone());
//         data.put("status", newUser.getStatus());
//         data.put("pic", newUser.getPic());
//         data.put("token", token);
//         return data;
//     }
//
//
// }
