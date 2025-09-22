package com.yinuo.flow.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;


/**
 * Describe:
 *
 * @author fxb
 * @date 2025/9/22
 */


@Data
@TableName("flow_data")
public class FlowData {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Integer meterId;     // 流量计编号
    private LocalDateTime timestamp; // 采集时间
    private Double massFlow;     // 质量流量 t/h
    private Double massTotal;    // 质量总量 t
    private Double volumeFlow;   // 体积流量 m3/h
    private Double volumeTotal;  // 体积总量 m3
    private Double density;      // 密度
    private Double temperature;  // 温度

    @TableField(exist = false)
    private boolean online; // 仅内存标记，不进数据库
}
