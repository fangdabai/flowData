package com.yinuo.flow.service;

import com.fazecast.jSerialComm.SerialPort;
import com.yinuo.flow.entity.FlowData;
import com.yinuo.flow.mapper.FlowDataMapper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

@Service
public class FlowMeterService {

    @Autowired
    private FlowDataMapper flowDataMapper;

    private SerialPort serialPort;
    private final String portName = "COM6";
    private final int baudRate = 9600;

    // 通讯状态（true=成功，false=失败），仅在内存维护
    private final Map<Integer, Boolean> commStatus = new ConcurrentHashMap<>();

    // 初始化串口
    public void initSerialPort() {
        if (serialPort != null && serialPort.isOpen()) {
            return;
        }
        serialPort = SerialPort.getCommPort(portName);
        serialPort.setComPortParameters(baudRate, 8, SerialPort.ONE_STOP_BIT, SerialPort.NO_PARITY);
        serialPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 1000, 0);

        if (!serialPort.openPort()) {
            System.err.println("[ERROR] 串口打开失败: " + portName);
            serialPort = null;
        } else {
            System.out.println("[INFO] 串口已打开: " + portName);
        }
    }

    @Scheduled(fixedRate = 60_000)
    public void collectData() {
        initSerialPort();
        if (serialPort == null) {
            System.err.println("[WARN] 串口未打开，跳过本次采集");
            return;
        }

        for (int meterId = 1; meterId <= 2; meterId++) {
            try {
                FlowData data = readMeterWithRetry(meterId, 3);
                if (data != null) {
                    flowDataMapper.insert(data);
                    commStatus.put(meterId, true); // 成功
                    System.out.println("[INFO] 流量计 " + meterId + " 数据采集完成: " + data);
                } else {
                    commStatus.put(meterId, false); // 失败
                    System.err.println("[WARN] 流量计 " + meterId + " 数据采集失败");
                }
            } catch (Exception e) {
                commStatus.put(meterId, false); // 异常也标记失败
                System.err.println("[ERROR] 流量计 " + meterId + " 异常: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    // 获取某个流量计通讯状态（前端可调用）
    public boolean isMeterOnline(int meterId) {
        return commStatus.getOrDefault(meterId, false);
    }

    // 带重试机制读取流量计
    private FlowData readMeterWithRetry(int meterId, int retryCount) throws Exception {
        for (int attempt = 1; attempt <= retryCount; attempt++) {
            try {
                FlowData data = readMeter(meterId);
                if (data != null) return data;
            } catch (Exception e) {
                System.err.println("[WARN] 第 " + attempt + " 次读取失败: " + e.getMessage());
                if (attempt == retryCount) throw e;
                if (serialPort != null) serialPort.closePort();
                serialPort = null;
                Thread.sleep(500);
                initSerialPort();
            }
        }
        return null;
    }

    // 读取流量计 40168~40179 数据
    private FlowData readMeter(int meterId) throws Exception {
        FlowData data = new FlowData();
        data.setMeterId(meterId);
        data.setTimestamp(LocalDateTime.now());

        // 40168 (0x00A7) 开始，共 12 个寄存器
        byte[] request = buildReadCommand(meterId, 0x00A7, 12);
        System.out.println("[DEBUG] 发送请求命令: " + bytesToHex(request));
        serialPort.getOutputStream().write(request);
        serialPort.getOutputStream().flush();

        InputStream in = serialPort.getInputStream();
        byte[] buffer = new byte[256];
        int expectedLen = 3 + 12 * 2 + 2; // 站号+功能码+字节数 + 数据24字节 + CRC2字节 = 29
        int len = readFully(in, buffer, expectedLen);
        System.out.println("[DEBUG] 接收到字节长度: " + len);

        if (len == expectedLen) {
            // 自动检测字节顺序解析浮点数
            data.setMassFlow(parseFloatAuto(buffer, 3));      // 40168-40169
            data.setDensity(parseFloatAuto(buffer, 7));       // 40170-40171
            data.setTemperature(parseFloatAuto(buffer, 11));  // 40172-40173
            data.setVolumeFlow(parseFloatAuto(buffer, 15));   // 40174-40175
            data.setMassTotal(parseFloatAuto(buffer, 19));    // 40176-40177
            data.setVolumeTotal(parseFloatAuto(buffer, 23));  // 40178-40179

            System.out.println("[DEBUG] 解析数据: MassFlow=" + data.getMassFlow() +
                    ", Density=" + data.getDensity() +
                    ", Temp=" + data.getTemperature() +
                    ", VolumeFlow=" + data.getVolumeFlow() +
                    ", MassTotal=" + data.getMassTotal() +
                    ", VolumeTotal=" + data.getVolumeTotal());
        } else {
            System.err.println("[WARN] 串口未返回有效数据，返回长度: " + len);
            return null;
        }

        return data;
    }

    // 循环读取直到收满预期长度
    private int readFully(InputStream in, byte[] buffer, int expectedLen) throws Exception {
        int total = 0;
        while (total < expectedLen) {
            int n = in.read(buffer, total, expectedLen - total);
            if (n < 0) break;
            total += n;
        }
        return total;
    }

    // 构造 Modbus RTU 读取命令
    private byte[] buildReadCommand(int meterId, int startRegister, int quantity) {
        byte[] cmd = new byte[8];
        cmd[0] = (byte) meterId;
        cmd[1] = 0x04;
        cmd[2] = (byte) (startRegister >> 8);
        cmd[3] = (byte) (startRegister & 0xFF);
        cmd[4] = (byte) (quantity >> 8);
        cmd[5] = (byte) (quantity & 0xFF);
        int crc = crc16(cmd, 6);
        cmd[6] = (byte) (crc & 0xFF);
        cmd[7] = (byte) (crc >> 8);
        return cmd;
    }

    // CRC16计算
    private int crc16(byte[] data, int len) {
        int crc = 0xFFFF;
        for (int j = 0; j < len; j++) {
            crc ^= (data[j] & 0xFF);
            for (int i = 0; i < 8; i++) {
                if ((crc & 0x1) != 0) {
                    crc >>= 1;
                    crc ^= 0xA001;
                } else {
                    crc >>= 1;
                }
            }
        }
        return crc;
    }

    // 自动检测字节顺序解析浮点数
    private double parseFloatAuto(byte[] buf, int offset) {
        double val1 = parseFloatFromBytes(buf, offset, false); // ABCD
        if (isValid(val1)) return val1;

        double val2 = parseFloatFromBytes(buf, offset, true);  // CDAB
        if (isValid(val2)) return val2;

        return val1; // 两种都不行，就返回原始
    }

    private boolean isValid(double v) {
        return !Double.isNaN(v) && !Double.isInfinite(v) &&
                Math.abs(v) < 1e8 && Math.abs(v) > 1e-8;
    }

    // 浮点数解析（支持寄存器对调）
    private double parseFloatFromBytes(byte[] buf, int offset, boolean swapRegisters) {
        if (offset + 3 >= buf.length) return 0.0;

        byte b1 = buf[offset];
        byte b2 = buf[offset + 1];
        byte b3 = buf[offset + 2];
        byte b4 = buf[offset + 3];

        if (swapRegisters) {
            byte t1 = b1, t2 = b2;
            b1 = b3; b2 = b4;
            b3 = t1; b4 = t2;
        }

        int intValue = ((b1 & 0xFF) << 24) |
                ((b2 & 0xFF) << 16) |
                ((b3 & 0xFF) << 8)  |
                (b4 & 0xFF);

        return Float.intBitsToFloat(intValue);
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            sb.append(String.format("%02X ", bytes[i]));
        }
        return sb.toString();
    }
}
