// FILE: src/main/java/com/scy/mytemplate/mapper/OrganizationMemberMapper.java
package com.scy.mytemplate.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.scy.mytemplate.model.entity.OrganizationMember;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * OrganizationMember 表的数据库操作接口。
 *
 * @author Bedrock
 */
public interface OrganizationMemberMapper extends BaseMapper<OrganizationMember> {

    /**
     * 查找指定用户在指定组织内的角色。
     */
    String findUserRoleInOrg(@Param("userId") Long userId, @Param("organizationId") Long organizationId);

    /**
     * 查找指定用户所属的所有组织的ID列表。
     */
    List<Long> findUserOrganizationIds(@Param("userId") Long userId);
}
// END OF FILE: src/main/java/com/scy/mytemplate/mapper/OrganizationMemberMapper.java