// 文件路径: src/main/java/com/scy/mytemplate/mapper/OrganizationMemberMapper.java
package com.scy.mytemplate.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.scy.mytemplate.model.entity.OrganizationMember;
import com.scy.mytemplate.model.entity.User; // <-- 需要引入User实体
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * OrganizationMember 表的数据库操作接口。
 */
public interface OrganizationMemberMapper extends BaseMapper<OrganizationMember> {

    /**
     * 查找指定用户在指定组织内的角色。
     */
    String findUserRoleInOrg(@Param("userId") String userId, @Param("organizationId") String organizationId);

    /**
     * 查找指定用户所属的所有组织的ID列表。
     */
    List<String> findUserOrganizationIds(@Param("userId") String userId);

    /**
     * --- 新增的方法 ---
     * 根据组织ID，查找该组织下的所有用户实体列表。
     * @param organizationId 目标组织的ID
     * @return 属于该组织的 User 对象列表
     */
    List<User> findUsersInOrganization(@Param("organizationId") String organizationId);

}