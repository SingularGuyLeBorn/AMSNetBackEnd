// FILE: src/main/java/com/scy/mytemplate/mapper/OrganizationMapper.java
package com.scy.mytemplate.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.scy.mytemplate.model.entity.Organization;
import com.scy.mytemplate.model.vo.UserOrganizationInfoVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Organization 表的数据库操作接口。
 *
 * @author Bedrock
 */
public interface OrganizationMapper extends BaseMapper<Organization> {

    /**
     * 根据用户ID查询其所属的所有组织及其在组织内的角色。
     */
    List<UserOrganizationInfoVO> findUserOrganizationsByUserId(@Param("userId") Long userId);
}
// END OF FILE: src/main/java/com/scy/mytemplate/mapper/OrganizationMapper.java