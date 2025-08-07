// 文件路径: src/main/java/com/scy/mytemplate/model/vo/OrganizationVO.java
package com.scy.mytemplate.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.scy.mytemplate.model.entity.Organization;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
import java.util.Date;

/**
 * 组织视图对象 (Organization View Object)。
 * 用于向前端返回组织的公开信息。
 *
 * @author Bedrock
 */
@Data
public class OrganizationVO implements Serializable {

    /**
     * 组织的唯一ID
     */
    private String id;

    /**
     * 组织名称
     */
    private String name;

    /**
     * 组织创建者的用户ID
     */
    private String ownerId;

    /**
     * 组织简介
     */
    private String description;

    /**
     * 组织的创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    /**
     * 将数据库实体对象 (Organization) 转换为前端视图对象 (OrganizationVO) 的静态工厂方法。
     *
     * @param organization 数据库查出的 Organization 实体
     * @return 转换后的 OrganizationVO 对象
     */
    public static OrganizationVO fromEntity(Organization organization) {
        if (organization == null) {
            return null;
        }
        OrganizationVO organizationVO = new OrganizationVO();
        BeanUtils.copyProperties(organization, organizationVO);
        return organizationVO;
    }

    private static final long serialVersionUID = 1L;
}