package com.example.cute_pet.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 
 * @TableName pet
 */
@TableName(value ="pet")
@Data
public class Pet implements Serializable {
    /**
     * 
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 宠物类别 1猫,2狗
     */
    private Integer petType;

    /**
     * 宠物品种
     */
    private String petVariety;

    /**
     * 宠物昵称
     */
    private String petNick;

    /**
     * 宠物性别 弟弟 妹妹
     */
    private String petSex;

    /**
     * 宠物年龄
     */
    private String petAge;

    /**
     * 宠物地址
     */
    private String petAddress;

    /**
     * 宠物状态
     */
    private String petStatus;

    /**
     * 配种宠物经验
     */
    private String experience;

    /**
     * 铲屎官的话
     */
    private String petOwner;

    /**
     * 宠物主人
     */
    private Integer userId;

    /**
     * 显示状态 0 草稿 1·2显示 3审核
     */
    private Integer status;

    /**
     * 创建时间
     */
    private Date createTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    @Override
    public boolean equals(Object that) {
        if (this == that) {
            return true;
        }
        if (that == null) {
            return false;
        }
        if (getClass() != that.getClass()) {
            return false;
        }
        Pet other = (Pet) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getPetType() == null ? other.getPetType() == null : this.getPetType().equals(other.getPetType()))
            && (this.getPetVariety() == null ? other.getPetVariety() == null : this.getPetVariety().equals(other.getPetVariety()))
            && (this.getPetNick() == null ? other.getPetNick() == null : this.getPetNick().equals(other.getPetNick()))
            && (this.getPetSex() == null ? other.getPetSex() == null : this.getPetSex().equals(other.getPetSex()))
            && (this.getPetAge() == null ? other.getPetAge() == null : this.getPetAge().equals(other.getPetAge()))
            && (this.getPetAddress() == null ? other.getPetAddress() == null : this.getPetAddress().equals(other.getPetAddress()))
            && (this.getPetStatus() == null ? other.getPetStatus() == null : this.getPetStatus().equals(other.getPetStatus()))
            && (this.getExperience() == null ? other.getExperience() == null : this.getExperience().equals(other.getExperience()))
            && (this.getPetOwner() == null ? other.getPetOwner() == null : this.getPetOwner().equals(other.getPetOwner()))
            && (this.getUserId() == null ? other.getUserId() == null : this.getUserId().equals(other.getUserId()))
            && (this.getStatus() == null ? other.getStatus() == null : this.getStatus().equals(other.getStatus()))
            && (this.getCreateTime() == null ? other.getCreateTime() == null : this.getCreateTime().equals(other.getCreateTime()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getPetType() == null) ? 0 : getPetType().hashCode());
        result = prime * result + ((getPetVariety() == null) ? 0 : getPetVariety().hashCode());
        result = prime * result + ((getPetNick() == null) ? 0 : getPetNick().hashCode());
        result = prime * result + ((getPetSex() == null) ? 0 : getPetSex().hashCode());
        result = prime * result + ((getPetAge() == null) ? 0 : getPetAge().hashCode());
        result = prime * result + ((getPetAddress() == null) ? 0 : getPetAddress().hashCode());
        result = prime * result + ((getPetStatus() == null) ? 0 : getPetStatus().hashCode());
        result = prime * result + ((getExperience() == null) ? 0 : getExperience().hashCode());
        result = prime * result + ((getPetOwner() == null) ? 0 : getPetOwner().hashCode());
        result = prime * result + ((getUserId() == null) ? 0 : getUserId().hashCode());
        result = prime * result + ((getStatus() == null) ? 0 : getStatus().hashCode());
        result = prime * result + ((getCreateTime() == null) ? 0 : getCreateTime().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", petType=").append(petType);
        sb.append(", petVariety=").append(petVariety);
        sb.append(", petNick=").append(petNick);
        sb.append(", petSex=").append(petSex);
        sb.append(", petAge=").append(petAge);
        sb.append(", petAddress=").append(petAddress);
        sb.append(", petStatus=").append(petStatus);
        sb.append(", experience=").append(experience);
        sb.append(", petOwner=").append(petOwner);
        sb.append(", userId=").append(userId);
        sb.append(", status=").append(status);
        sb.append(", createTime=").append(createTime);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}