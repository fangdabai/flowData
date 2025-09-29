package com.yinuo.flow.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.yinuo.flow.entity.FlowData;
import com.yinuo.flow.mapper.FlowDataMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Describe:
 *
 * @author fxb
 * @date 2025/9/22
 */


@Service
public class FlowDataService {

    @Autowired
    private FlowDataMapper flowDataMapper;

    @Autowired
    private FlowMeterService flowMeterService; // 注入 FlowMeterService

    // 每分钟采集一次（2个流量计）-- 模拟数据
    @Scheduled(fixedRate = 60_000)
    public void collectData() {
        for (int meterId = 1; meterId <= 2; meterId++) {
            FlowData data = new FlowData();
            data.setTimestamp(LocalDateTime.now());
            data.setMassFlow(Math.random() * 100);
            data.setMassTotal(Math.random() * 5000);
            data.setVolumeFlow(Math.random() * 80);
            data.setVolumeTotal(Math.random() * 4000);
            data.setDensity(800 + Math.random() * 20);
            data.setTemperature(20 + Math.random() * 5);
            data.setMeterId(meterId);
            flowDataMapper.insert(data);
        }

    }










    // 1. 获取实时最新数据 + 通讯状态
    public FlowData getCurrentData(int meterId) {
        List<FlowData> list = flowDataMapper.selectList(
                new QueryWrapper<FlowData>()
                        .eq("meter_id", meterId)
                        .orderByDesc("timestamp")
                        .last("limit 1")
        );
        if (list.isEmpty()) {
            return null;
        }

        FlowData data = list.get(0);
        // 给 online 字段赋值（不会入库）
        data.setOnline(flowMeterService.isMeterOnline(meterId));
        return data;
    }


    // 2. 获取最近 N 条数据
    public List<FlowData> getRecentData(int meterId, int limit) {
        return flowDataMapper.selectList(
                new QueryWrapper<FlowData>()
                        .eq("meter_id", meterId)
                        .orderByDesc("timestamp")
                        .last("limit " + limit)
        );
    }


    // 3. 根据时间范围查询数据
    public List<FlowData> getRangeData(int meterId, LocalDateTime startTime, LocalDateTime endTime) {
        return flowDataMapper.selectList(
                new QueryWrapper<FlowData>()
                        .eq("meter_id", meterId)
                        .between("timestamp", startTime, endTime)
                        .orderByAsc("timestamp")
        );
    }


    // 4. 根据时间范围查询 质量总量差值和体积总量差值（包含起点终点记录）
    public Map<String, Object> getSummary(int meterId, LocalDateTime startTime, LocalDateTime endTime) {
        // 获取开始时间之前的最新记录
        FlowData startData = flowDataMapper.selectOne(
                new QueryWrapper<FlowData>()
                        .eq("meter_id", meterId)
                        .le("timestamp", startTime)
                        .orderByDesc("timestamp")
                        .last("limit 1")
        );

        // 获取结束时间之前的最新记录
        FlowData endData = flowDataMapper.selectOne(
                new QueryWrapper<FlowData>()
                        .eq("meter_id", meterId)
                        .le("timestamp", endTime)
                        .orderByDesc("timestamp")
                        .last("limit 1")
        );

        Map<String, Object> result = new HashMap<>();
        result.put("meterId", meterId);
        result.put("startTime", startTime);
        result.put("endTime", endTime);

        if (startData != null) {
            Map<String, Object> startInfo = new HashMap<>();
            startInfo.put("timestamp", startData.getTimestamp());
            startInfo.put("massTotal", startData.getMassTotal());
            startInfo.put("volumeTotal", startData.getVolumeTotal());
            result.put("startRecord", startInfo);
        }

        if (endData != null) {
            Map<String, Object> endInfo = new HashMap<>();
            endInfo.put("timestamp", endData.getTimestamp());
            endInfo.put("massTotal", endData.getMassTotal());
            endInfo.put("volumeTotal", endData.getVolumeTotal());
            result.put("endRecord", endInfo);
        }

        if (startData != null && endData != null) {
            result.put("massTotalDiff", endData.getMassTotal() - startData.getMassTotal());
            result.put("volumeTotalDiff", endData.getVolumeTotal() - startData.getVolumeTotal());
        } else {
            result.put("massTotalDiff", null);
            result.put("volumeTotalDiff", null);
        }

        return result;
    }

}


