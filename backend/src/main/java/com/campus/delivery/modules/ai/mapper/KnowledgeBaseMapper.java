package com.campus.delivery.modules.ai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.delivery.modules.ai.entity.KnowledgeBase;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 知识库数据访问。主责：成员3。
 */
@Mapper
public interface KnowledgeBaseMapper extends BaseMapper<KnowledgeBase> {

    /**
     * 关键词召回：按问题与关键词做模糊匹配，按命中次数排序。
     *
     * <p>当前为关键词召回，后续可替换为向量检索；接口保持不变。
     */
    @Select("SELECT * FROM knowledge_base WHERE status = 1 "
            + "AND (question LIKE CONCAT('%', #{keyword}, '%') "
            + "  OR keywords LIKE CONCAT('%', #{keyword}, '%') "
            + "  OR answer LIKE CONCAT('%', #{keyword}, '%')) "
            + "ORDER BY hit_count DESC LIMIT #{limit}")
    List<KnowledgeBase> searchByKeyword(@Param("keyword") String keyword, @Param("limit") int limit);
}
