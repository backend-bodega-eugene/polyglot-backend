package com.eugene.goalhub.admin.controller;

import com.eugene.goalhub.admin.service.AdminMatchMarketOptionService;
import dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import response.Result;

/**
 * 后台赛事玩法赔率管理接口。
 *
 * <p>维护具体赛事下的玩法选项与赔率数据。</p>
 */
@Tag(name = "后台赛事玩法赔率管理", description = "后台赛事玩法赔率分页查询、新增、修改和删除接口")
@RestController
@RequestMapping("/admin/matchmarketoption")
public class AdminMatchMarketOptionController {

    /**
     * 后台赛事玩法赔率服务。
     */
    private final AdminMatchMarketOptionService adminMatchMarketOptionService;

    /**
     * 创建后台赛事玩法赔率管理接口实例。
     *
     * @param adminMatchMarketOptionService 后台赛事玩法赔率服务
     */
    public AdminMatchMarketOptionController(
            AdminMatchMarketOptionService adminMatchMarketOptionService) {
        this.adminMatchMarketOptionService = adminMatchMarketOptionService;
    }

    /**
     * 分页查询赛事玩法赔率。
     *
     * @param request 赛事玩法赔率分页查询条件
     * @return 赛事玩法赔率分页数据
     */
    @Operation(summary = "分页查询赛事玩法赔率", description = "分页查询赛事玩法赔率列表。")
    @PostMapping("/page")
    public Result<PageResponse<MatchMarketOptionResponse>> page(
            @Parameter(description = "赛事玩法赔率分页查询参数", required = true)
            @RequestBody MatchMarketOptionPageRequest request) {

        return Result.success(
                adminMatchMarketOptionService.page(request));
    }

    /**
     * 新增赛事玩法赔率。
     *
     * @param request 赛事玩法赔率新增参数
     * @return 空结果
     */
    @Operation(summary = "新增赛事玩法赔率", description = "为赛事新增玩法子项赔率。")
    @PostMapping("/add")
    public Result<Void> add(
            @Parameter(description = "赛事玩法赔率新增参数", required = true)
            @RequestBody AddMatchMarketOptionRequest request) {

        adminMatchMarketOptionService.add(request);

        return Result.success();
    }

    /**
     * 更新赛事玩法赔率。
     *
     * @param request 赛事玩法赔率更新参数
     * @return 空结果
     */
    @Operation(summary = "更新赛事玩法赔率", description = "更新赛事玩法赔率。")
    @PostMapping("/update")
    public Result<Void> update(
            @Parameter(description = "赛事玩法赔率更新参数", required = true)
            @RequestBody UpdateMatchMarketOptionRequest request) {

        adminMatchMarketOptionService.update(request);

        return Result.success();
    }

    /**
     * 删除赛事玩法赔率。
     *
     * @param request 赛事玩法赔率删除参数
     * @return 空结果
     */
    @Operation(summary = "删除赛事玩法赔率", description = "删除指定赛事玩法赔率。")
    @PostMapping("/delete")
    public Result<Void> delete(
            @Parameter(description = "赛事玩法赔率删除参数", required = true)
            @RequestBody DeleteMatchMarketOptionRequest request) {

        adminMatchMarketOptionService.delete(request);

        return Result.success();
    }
}
