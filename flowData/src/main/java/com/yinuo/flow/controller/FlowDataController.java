package com.yinuo.flow.controller;

import com.yinuo.flow.entity.FlowData;
import com.yinuo.flow.mapper.FlowDataMapper;
import com.yinuo.flow.service.FlowDataService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

/**
 * Describe:
 *
 * @author fxb
 * @date 2025/9/22
 */


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/flow")
public class FlowDataController {
    private final FlowDataService service;

    public FlowDataController(FlowDataService service) {
        this.service = service;
    }


    // 1. 获取指定流量计实时最新数据
    @GetMapping("/{meterId}/current")
    public FlowData getCurrent(@PathVariable int meterId) {
        return service.getCurrentData(meterId);
    }

    // 2. 获取指定流量计最近数据
    @GetMapping("/{meterId}/recent")
    public List<FlowData> getRecent(@PathVariable int meterId, @RequestParam(defaultValue = "100") int limit) {
        return service.getRecentData(meterId, limit);
    }

    // 3. 根据时间范围获取指定流量计数据
    @GetMapping("/{meterId}/range")
    public List<FlowData> getRange(
            @PathVariable int meterId,
            @RequestParam String start,
            @RequestParam String end
    ) {
        LocalDateTime startTime = LocalDateTime.parse(start);
        LocalDateTime endTime = LocalDateTime.parse(end);
        return service.getRangeData(meterId, startTime, endTime);
    }

    // 4. 根据时间范围查询 质量总量差值和体积总量差值
    @GetMapping("/summary")
    public Map<String, Object> getSummary(
            @RequestParam int meterId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {

        return service.getSummary(meterId, startTime, endTime);
    }





}



