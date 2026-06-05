package com.eugene.goalhub.match.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.eugene.goalhub.match.entity.SoccerTeamEntity;
import dto.AdminTeamResponse;
import dto.TeamPageRequest;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 足球球队 Mapper。
 *
 * <p>负责足球球队基础表的通用 CRUD 和后台球队分页查询。</p>
 */
@Mapper
public interface SoccerTeamMapper extends BaseMapper<SoccerTeamEntity> {

    /**
     * 分页查询后台球队列表。
     *
     * @param page    分页参数
     * @param request 查询条件
     * @return 后台球队分页数据
     */
    @Select("""
            <script>
            SELECT
                t.id,
                t.code,
                t.logo_url AS logoUrl,
                t.status,
                i.name,
                i.short_name AS shortName,
                t.created_at AS createdAt,
                t.updated_at AS updatedAt
            FROM soccer_team t
            LEFT JOIN soccer_team_i18n i
                ON i.team_id = t.id
               AND i.lang_code = #{req.langCode}
            WHERE 1 = 1
            <if test="req.keyword != null and req.keyword != ''">
                AND (
                    i.name LIKE CONCAT('%', #{req.keyword}, '%')
                    OR i.short_name LIKE CONCAT('%', #{req.keyword}, '%')
                    OR t.code LIKE CONCAT('%', #{req.keyword}, '%')
                )
            </if>
            ORDER BY t.id DESC
            </script>
            """)
    Page<AdminTeamResponse> selectTeamPage(
            Page<AdminTeamResponse> page,
            @Param("req") TeamPageRequest request
    );
}
