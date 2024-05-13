// package com.example.cute_pet.controller;
//
// import com.auth0.jwt.interfaces.DecodedJWT;
// import com.baomidou.mybatisplus.core.conditions.Wrapper;
// import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
// import com.baomidou.mybatisplus.core.toolkit.StringUtils;
// import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
// import com.example.cute_pet.domain.*;
// import com.example.cute_pet.service.*;
// import com.example.cute_pet.util.ImageUtil;
// import com.example.cute_pet.util.TokenUtils;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.util.FileSystemUtils;
// import org.springframework.web.bind.annotation.*;
// import org.springframework.web.multipart.MultipartFile;
//
// import java.io.File;
// import java.sql.Timestamp;
// import java.text.SimpleDateFormat;
// import java.util.*;
//
// @RestController
// @RequestMapping("/pet")
// public class PetCardController {
//     @Autowired
//     private PetCardService petCardService;
//     @Autowired
//     private PetPicService petPicService;
//     @Autowired
//     private PetThumbService petThumbService;
//     @Autowired
//     private PetCommentService petCommentService;
//     @Autowired
//     private UserService userService;
//
//
//     @GetMapping("test")
//     public Map<String, Object> Test() {
//
//         Map<String, Object> map = new HashMap<>();
//         map.put("code", 1);
//         map.put("result", "pageList");
//         map.put("msg", "请求成功！");
//         return map;
//     }
//
//     /**
//      * 添加修改宠物信息卡
//      * pram{petCard,token}
//      */
//     @PostMapping("save")
//     public Map<String, Object> savePetCard(@RequestBody PetCard petCard,
//                                            @RequestParam("isOpImg") Boolean isOpImg,
//                                            @RequestHeader("token") String token) {
//         Map<String, Object> map = new HashMap<>();
//         Map<String, Object> data = new HashMap<>();
//
//         Integer userId = TokenUtils.getDecodedJWT(token).getClaim("id").asInt();
//         // id 空，添加宠物的信息表返回宠物信息的id和用户id
//         if (petCard.getId() == null) {
//             petCard.setStatus(0);
//             petCard.setUserId(userId);
//             System.out.println(petCard);
//             boolean saved = petCardService.save(petCard);
//             if (saved) {
//                 data.put("petId", petCard.getId());
//                 data.put("userId", petCard.getUserId());
//                 map.put("code", 1);
//                 map.put("result", data);
//                 map.put("msg", "请求成功！");
//                 return map;
//             } else {
//                 map.put("code", 0);
//                 map.put("result", data);
//                 map.put("msg", "添加失败！");
//                 return map;
//             }
//         } else {
//             // 修改
//             if (Objects.equals(petCard.getUserId(), userId)) {
//                 // 删除之前的图片
//                 if (isOpImg) {
//                     // 自定义查询用户是否已注册
//                     QueryWrapper<PetPic> petIdWrapper = new QueryWrapper<PetPic>().eq("pet_id", petCard.getId());
//                     List<PetPic> list = petPicService.list(petIdWrapper);
//                     list.forEach(item -> {
//                         // 1.获取数据库当前的图片地址
//                         System.out.println("item" + item.getId());
//                         String oldImagePath = "";
//                         String oldPrc = petPicService.getById(item.getId()).getPic();
//                         if (!StringUtils.isEmpty(oldPrc)) {
//                             String oldFileName = oldPrc.substring(oldPrc.indexOf("image/") + 6);
//                             oldImagePath = ImageUtil.getNewImagePath(oldFileName, "image/");
//                             System.out.println("oldImagePath" + oldImagePath);
//                             // 删除图片 判断空不能删除
//                             if (!oldImagePath.isEmpty()) {
//                                 File file = new File(oldImagePath);
//                                 if (file.exists()) {
//                                     FileSystemUtils.deleteRecursively(file);
//                                 } else {
//                                     System.out.println("目标文件不存在");
//                                 }
//                             }
//                         }
//                         // 根据id照片的删除记录
//                         boolean b = petPicService.removeById(item.getId());
//                         System.out.println("删除记录" + b);
//                     });
//                 }
//
//                 // 修改宠物信息
//                 petCardService.updateById(petCard);
//                 data.put("petId", petCard.getId());
//                 data.put("userId", petCard.getUserId());
//                 map.put("code", 1);
//                 map.put("result", data);
//                 map.put("msg", "请求成功！");
//                 return map;
//             } else {
//                 map.put("code", 0);
//                 map.put("result", data);
//                 map.put("msg", "修改失败！");
//                 return map;
//             }
//         }
//     }
//
//     /**
//      * 添加宠物图片
//      */
//     @PostMapping("upload/img")
//     public Map<String, Object> addImage(@RequestBody MultipartFile files,
//                                         @RequestParam("petId") int petId,
//                                         @RequestParam("userId") int userId) {
//         Map<String, Object> map = new HashMap<>();
//         if (!files.isEmpty()) {
//             // 保存文件操作
//             // 获取文件后缀
//             String suffixName = ImageUtil.getImagePath(files);
//             // 生成新文件名称
//             String newFileName = ImageUtil.getNewFileName(suffixName);
//             // 保存文件 生成保存文件的地址
//             File file = new File(ImageUtil.getNewImagePath(newFileName, "image/"));
//             // 保存成功
//             boolean state = ImageUtil.saveImage(files, file);
//             // 保存位置
//             if (state) {
//                 // 修改头像地址
//                 PetPic petPic = new PetPic();
//                 petPic.setPetId(petId);
//                 petPic.setUserId(userId);
//                 petPic.setPic("http://localhost:8088/image/" + newFileName);
//                 boolean saved = petPicService.save(petPic);
//                 if (!saved) {
//                     // 保存图片地址到数据库 失败就删除图片文件
//                     if (file.exists()) {
//                         FileSystemUtils.deleteRecursively(file);
//                     } else {
//                         System.out.println("目标文件不存在");
//                     }
//
//                     map.put("code", 1);
//                     map.put("msg", "图片上传失败");
//                     return map;
//
//                 }
//                 System.out.println("添加了图片" + petPic);
//             }
//         }
//
//         map.put("code", 1);
//         map.put("msg", "添加图片成功！");
//         return map;
//     }
//
//
//     /**
//      * 查询个人所有宠物
//      */
//     @GetMapping("query/list")
//     public Map<String, Object> queryPetUserId(@RequestParam("uid") int uid, @RequestHeader("token") String token) {
//
//         // 宠物信息的数组
//         QueryWrapper<PetCard> findById = new QueryWrapper<PetCard>().eq("user_id", uid);
//         List<PetCard> petCardList = petCardService.list(findById);
//         ArrayList<Object> dataList = mapPet(token, petCardList);
//         Map<String, Object> map = new HashMap<>();
//         map.put("code", 1);
//         map.put("result", dataList);
//         map.put("msg", "请求成功");
//         return map;
//     }
//
//     /**
//      * 查看一个宠物
//      */
//     @GetMapping("query/item")
//     public Map<String, Object> queryByPetId(@RequestParam("pid") int pid, @RequestHeader("token") String token) {
//         // 宠物信息对象
//         PetCard petCard = petCardService.getById(pid);
//         // 宠物图片数组
//         ArrayList<Object> list = new ArrayList<>();
//         QueryWrapper<PetPic> petId = new QueryWrapper<PetPic>().eq("pet_id", pid);
//         List<PetPic> petPicList = petPicService.list(petId);
//         petPicList.forEach(item -> {
//             list.add(item.getPic());
//         });
//
//         // 获取点赞相关
//         HashMap<String, Object> like = new HashMap<>();
//         // 获取点赞数量
//         QueryWrapper<PetThumb> pId = new QueryWrapper<PetThumb>().eq("pet_id", pid);
//         int count = petThumbService.list(pId).size();
//         like.put("count", count);
//         // 用户是否点赞
//         if (TokenUtils.verify(token)) {
//             QueryWrapper<PetThumb> eqT = new QueryWrapper<PetThumb>().eq("pet_id", pid).eq("user_id", TokenUtils.getDecodedJWT(token).getClaim("id").asInt());
//             PetThumb isThumb = petThumbService.getOne(eqT, false);
//             System.out.println("测试" + isThumb);
//             if (isThumb == null) {
//                 like.put("isLike", false);
//             } else {
//                 like.put("isLike", true);
//             }
//         } else {
//             like.put("isLike", false);
//         }
//
//         // 单个宠物的信息
//         HashMap<String, Object> data = new HashMap<>();
//         data.put("petCard", petCard);
//         data.put("petPic", list);
//         data.put("petLike", like);
//
//         Map<String, Object> map = new HashMap<>();
//         map.put("code", 1);
//         map.put("msg", "查询成功");
//         map.put("result", data);
//         return map;
//     }
//
//     /**
//      * 宠物喜欢点赞模块
//      */
//     @PostMapping("item/like")
//     public HashMap<String, Object> petLike(@RequestBody HashMap<String, Object> req,
//                                            @RequestHeader("token") String token) {
//
//         HashMap<String, Object> map = new HashMap<>();
//         Integer userId = TokenUtils.getDecodedJWT(token).getClaim("id").asInt();
//         PetThumb petThumb = new PetThumb();
//         petThumb.setPetId((Integer) req.get("pid"));
//         petThumb.setUserId(userId);
//
//         QueryWrapper<PetThumb> eq = new QueryWrapper<PetThumb>()
//                 .eq("pet_id", req.get("pid"))
//                 .eq("user_id", userId);
//
//         PetThumb one = petThumbService.getOne(eq);
//         if (one != null) {
//             boolean removed = petThumbService.remove(eq);
//             if (removed) {
//                 map.put("code", 1);
//             } else {
//                 map.put("code", 0);
//             }
//         } else {
//             boolean saved = petThumbService.save(petThumb);
//             if (saved) {
//                 map.put("code", 1);
//             } else {
//                 map.put("code", 0);
//             }
//         }
//         map.put("msg", "操作成功");
//         return map;
//     }
//
//     /**
//      * 获取宠物评论区数据
//      */
//     @GetMapping("comment/list")
//     public HashMap<String, Object> commentList(@RequestParam("pid") int pid) {
//         HashMap<String, Object> map = new HashMap<>();
//
//         // 获取评论数量
//         QueryWrapper<PetComment> petId = new QueryWrapper<PetComment>().eq("pet_id", pid);
//         int count = petCommentService.list(petId).size();
//
//
//         QueryWrapper<PetComment> eq = new QueryWrapper<PetComment>().eq("pet_id", pid).eq("level", 0);
//         List<PetComment> comList = petCommentService.list(eq);
//
//         ArrayList<Object> objList = new ArrayList<>();
//         comList.forEach(item -> {
//             HashMap<String, Object> obj = new HashMap<>();
//             obj.put("id", item.getId());
//             obj.put("content", item.getContent());
//             obj.put("nickName", userService.getById(item.getUserId()).getNickname());
//             obj.put("userId", item.getUserId());
//             obj.put("level", item.getLevel());
//             obj.put("createTime", item.getCreateTime());
//             obj.put("avatar", userService.getById(item.getUserId()).getPic());
//
//             // child的数组里的对象
//             QueryWrapper<PetComment> eq1 = new QueryWrapper<PetComment>().eq("root_id", item.getId());
//
//             List<PetComment> childList = petCommentService.list(eq1);
//             System.out.println(childList);
//             ArrayList<Object> childArray = new ArrayList<>();
//             childList.forEach(cItem -> {
//                 HashMap<String, Object> childObj = new HashMap<>();
//                 childObj.put("id", cItem.getId());  // 评论id
//                 childObj.put("content", cItem.getContent()); // 评论内容
//                 childObj.put("userId", cItem.getUserId());  // 评论用户id
//                 childObj.put("nickName", userService.getById(cItem.getUserId()).getNickname()); // 评论用户昵称
//                 childObj.put("avatar", userService.getById(cItem.getUserId()).getPic());
//                 childObj.put("level", cItem.getLevel());  // 评论层级
//
//                 childObj.put("createTime", cItem.getCreateTime());// 评论时间
//                 childObj.put("rootId", cItem.getRootId()); // 根评论id，没有为null
//                 if (cItem.getParentId() != null) {
//                     childObj.put("parentId", cItem.getParentId()); // 回复目标用户的id
//                     childObj.put("replyUserName", userService.getById(cItem.getParentId()).getNickname()); // 回复目标用户的昵称
//                 }
//                 childArray.add(childObj);
//             });
//
//             obj.put("child", childArray);
//             objList.add(obj);
//         });
//         HashMap<Object, Object> data = new HashMap<>();
//         data.put("count", count);
//         data.put("comments", objList);
//
//         map.put("result", data);
//         map.put("code", 1);
//         map.put("msg", "操作成功");
//         return map;
//     }
//
//     /**
//      * 发表评论
//      */
//     @PostMapping("comment/save")
//     public HashMap<String, Object> test(@RequestBody PetComment petComment, @RequestHeader("token") String token) {
//         System.out.println(petComment);
//         petComment.setUserId(TokenUtils.getDecodedJWT(token).getClaim("id").asInt());
//         HashMap<String, Object> map = new HashMap<>();
//         boolean saved = petCommentService.save(petComment);
//         if (saved) {
//             map.put("code", 1);
//             map.put("msg", "评论成功");
//         } else {
//             map.put("code", 0);
//             map.put("msg", "评论失败");
//         }
//         return map;
//     }
//
//     /**
//      * 获取个人喜欢的列表
//      */
//     @GetMapping("like/list")
//     public HashMap<String, Object> likeList(@RequestHeader("token") String token) {
//         // 查询喜欢列表数据库的宠物id
//         QueryWrapper<PetThumb> uid = new QueryWrapper<PetThumb>().eq("user_id", TokenUtils.getDecodedJWT(token).getClaim("id").asInt());
//         List<PetThumb> likelist = petThumbService.list(uid);
//         // 创建数组集合，保存所有宠物信息
//         ArrayList<Object> dataList = null;
//         if (!likelist.isEmpty()) {
//             ArrayList<Integer> arr = new ArrayList<>();
//             likelist.forEach(item -> {
//                 arr.add(item.getPetId());
//             });
//             // 通过查询到的数组遍历id查询喜欢的宠物，列表查询
//             List<PetCard> petCards = petCardService.listByIds(arr);
//
//             dataList = mapPet(token, petCards);
//         }
//
//         HashMap<String, Object> map = new HashMap<>();
//         map.put("code", 1);
//         map.put("result", dataList);
//         map.put("msg", "请求成功");
//         return map;
//     }
//
//     private  ArrayList<Object> mapPet(@RequestHeader("token") String token, List<PetCard> petCards) {
//         ArrayList<Object> dataList = new ArrayList<>();
//         for (PetCard petCard : petCards) {
//             // 获取图片数组
//             ArrayList<Object> picList = new ArrayList<>();
//             QueryWrapper<PetPic> picPid = new QueryWrapper<PetPic>().eq("pet_id", petCard.getId());
//             List<PetPic> petPicList = petPicService.list(picPid);
//             petPicList.forEach(item -> {
//                 picList.add(item.getPic());
//             });
//
//             // 获取评论
//             // 获取评论数量
//             QueryWrapper<PetComment> commentPid = new QueryWrapper<PetComment>().eq("pet_id", petCard.getId());
//             int comCount = petCommentService.list(commentPid).size();
//
//             QueryWrapper<PetComment> commentWp = new QueryWrapper<PetComment>().eq("pet_id", petCard.getId()).eq("level", 0);
//             List<PetComment> comList = petCommentService.list(commentWp);
//
//             ArrayList<Object> objList = new ArrayList<>();
//             comList.forEach(item -> {
//                 HashMap<String, Object> obj = new HashMap<>();
//                 obj.put("id", item.getId());
//                 obj.put("content", item.getContent());
//                 obj.put("nickName", userService.getById(item.getUserId()).getNickname());
//                 obj.put("userId", item.getUserId());
//                 obj.put("level", item.getLevel());
//                 obj.put("createTime", item.getCreateTime());
//                 obj.put("avatar", userService.getById(item.getUserId()).getPic());
//
//                 // child的数组里的对象
//                 QueryWrapper<PetComment> eq1 = new QueryWrapper<PetComment>().eq("root_id", item.getId());
//
//                 List<PetComment> childList = petCommentService.list(eq1);
//                 System.out.println(childList);
//                 ArrayList<Object> childArray = new ArrayList<>();
//                 childList.forEach(cItem -> {
//                     HashMap<String, Object> childObj = new HashMap<>();
//                     childObj.put("id", cItem.getId());  // 评论id
//                     childObj.put("content", cItem.getContent()); // 评论内容
//                     childObj.put("userId", cItem.getUserId());  // 评论用户id
//                     childObj.put("nickName", userService.getById(cItem.getUserId()).getNickname()); // 评论用户昵称
//                     childObj.put("avatar", userService.getById(cItem.getUserId()).getPic());
//                     childObj.put("level", cItem.getLevel());  // 评论层级
//
//                     childObj.put("createTime", cItem.getCreateTime());// 评论时间
//                     childObj.put("rootId", cItem.getRootId()); // 根评论id，没有为null
//                     if (cItem.getParentId() != null) {
//                         childObj.put("parentId", cItem.getParentId()); // 回复目标用户的id
//                         childObj.put("replyUserName", userService.getById(cItem.getParentId()).getNickname()); // 回复目标用户的昵称
//                     }
//                     childArray.add(childObj);
//                 });
//
//                 obj.put("child", childArray);
//                 objList.add(obj);
//             });
//             HashMap<Object, Object> commentData = new HashMap<>();
//             commentData.put("count", comCount);
//             commentData.put("comments", objList);
//             // end
//
//             // 获取点赞相关
//             HashMap<String, Object> likeObj = new HashMap<>();
//             // 获取点赞数量
//             QueryWrapper<PetThumb> pId = new QueryWrapper<PetThumb>().eq("pet_id", petCard.getId());
//             int Tcount = petThumbService.list(pId).size();
//             likeObj.put("count", Tcount);
//             // 用户是否点赞
//             if (TokenUtils.verify(token)) {
//                 QueryWrapper<PetThumb> eqT = new QueryWrapper<PetThumb>().eq("pet_id", petCard.getId()).eq("user_id", TokenUtils.getDecodedJWT(token).getClaim("id").asInt());
//                 PetThumb isThumb = petThumbService.getOne(eqT, false);
//                 System.out.println("测试" + isThumb);
//                 if (isThumb == null) {
//                     likeObj.put("isLike", false);
//                 } else {
//                     likeObj.put("isLike", true);
//                 }
//             } else {
//                 likeObj.put("isLike", false);
//             }
//
//             HashMap<String, Object> petItem = new HashMap<>();
//             petItem.put("petCard", petCard);
//             petItem.put("petPic", picList);
//             petItem.put("petLike", likeObj);
//             petItem.put("comment",commentData);
//             dataList.add(petItem);
//         }
//         return dataList;
//     }
//
//     /**
//      * 删除宠物 Param{pid}代token
//      */
//     @DeleteMapping("delete")
//     public HashMap<String, Object> deletePet(@RequestBody HashMap<String, Object> res, @RequestHeader("token") String token) {
//         int pid = (int) res.get("pid");
//         HashMap<String, Object> map = new HashMap<>();
//         // 根据宠物id查询宠物信息
//         PetCard card = petCardService.getById(pid);
//         DecodedJWT decodedJWT = TokenUtils.getDecodedJWT(token);
//         // 判断删除的数据和用户是否一样，或者是管理员
//         if (Objects.equals(card.getUserId(), decodedJWT.getClaim("id").asInt()) || Objects.equals(decodedJWT.getClaim("role").asString(), "ADMIN")) {
//
//             // 1.删除评论
//             QueryWrapper<PetComment> petCommentPid = new QueryWrapper<PetComment>().eq("pet_id", pid);
//             boolean petCommentRemoved = petCommentService.remove(petCommentPid);
//
//             // 2.删除图片
//             QueryWrapper<PetPic> petPicPid = new QueryWrapper<PetPic>().eq("pet_id", pid);
//             // 2.1查询所有图片
//             List<PetPic> picList = petPicService.list(petPicPid);
//             picList.forEach(item -> {
//                 // 2.2.获取数据库当前的图片地址
//                 System.out.println("item" + item.getId());
//                 String oldImagePath = "";
//                 String oldPrc = petPicService.getById(item.getId()).getPic();
//                 if (!StringUtils.isEmpty(oldPrc)) {
//                     String oldFileName = oldPrc.substring(oldPrc.indexOf("image/") + 6);
//                     oldImagePath = ImageUtil.getNewImagePath(oldFileName, "image/");
//                     System.out.println("oldImagePath" + oldImagePath);
//                     // 2.3删除图片 判断空不能删除
//                     if (!oldImagePath.isEmpty()) {
//                         File file = new File(oldImagePath);
//                         if (file.exists()) {
//                             FileSystemUtils.deleteRecursively(file);
//                         } else {
//                             System.out.println("目标文件不存在");
//                         }
//                     }
//                 }
//                 // 2.4 根据id删除图片表记录
//                 boolean b = petPicService.removeById(item.getId());
//                 System.out.println("删除记录" + b);
//             });
//
//             // 3.删除点赞
//             QueryWrapper<PetThumb> petThumbPid = new QueryWrapper<PetThumb>().eq("pet_id", pid);
//             boolean petThumbRemoved = petThumbService.remove(petThumbPid);
//
//             // 4.删除宠物
//             boolean petCardRemoved = petCardService.removeById(pid);
//             map.put("code", 1);
//             map.put("msg", "删除成功");
//         } else {
//             map.put("code", 0);
//             map.put("msg", "删除失败");
//         }
//         return map;
//     }
//
//
//     /**
//      * 首页按条件查询宠物
//      */
//
//     @PostMapping("list")
//     public HashMap<String, Object> getIndexPetList(@RequestBody HashMap<String, Object> reqData, @RequestHeader("token") String token) {
//
//         QueryWrapper<PetCard> petCardQueryWrapper = new QueryWrapper<>();
//         if (reqData.containsKey("petType") && reqData.get("petType") != null) {
//             petCardQueryWrapper.like("pet_type", reqData.get("petType"));
//         }
//         if (reqData.containsKey("address") && reqData.get("address") != null) {
//
//             petCardQueryWrapper.like("pet_address", reqData.get("address"));
//         }
//         if (reqData.containsKey("status") && reqData.get("status") != null) {
//             petCardQueryWrapper.like("pet_status", reqData.get("status"));
//         }
//         if (reqData.containsKey("sex") && reqData.get("sex") != null) {
//             // 执行相应的逻辑
//             petCardQueryWrapper.like("pet_sex", reqData.get("sex"));
//         }
//         if (reqData.containsKey("age") && reqData.get("age") != null) {
//             // 执行相应的逻辑
//             petCardQueryWrapper.like("pet_age", reqData.get("age"));
//         }
//
//
//         Object currentObj = reqData.get("current");
//         Object sizeObj = reqData.get("size");
//
//         int current = toInt(currentObj);
//         int size = toInt(sizeObj);
//
//
//         Page<PetCard> page = new Page<>(current, size);
//         Page<PetCard> pageList = petCardService.page(page, petCardQueryWrapper);
//         // 处理后的宠物信息列表
//         ArrayList<Object> petList = mapPet(token, pageList.getRecords());
//
//         HashMap<String, Object> petObj = new HashMap<>();
//         petObj.put("petList",petList);
//         petObj.put("total",pageList.getTotal());
//         petObj.put("size",pageList.getSize());
//         petObj.put("current",pageList.getCurrent());
//         petObj.put("pages",pageList.getPages());
//
//
//         HashMap<String, Object> data = new HashMap<>();
//         data.put("code",1);
//         data.put("result", petObj);
//         data.put("msg","请求成功");
//         return data;
//     }
//
//     // 转数字类型
//     public int toInt(Object num) {
//         int data = 0;
//         if (num != null) {
//
//             try {
//                 data = Integer.parseInt(num.toString());
//                 // 这里的 currentInt 就是转换后的整数类型
//             } catch (NumberFormatException e) {
//                 // 处理转换异常
//                 System.out.println(e);
//             }
//         }
//         return data;
//     }
// }
