#include "grayscale.h"

namespace eugene
{
    // 将一张 RGBA 图像转换为灰度图
    // src  : 原始图像像素数据（RGBA，每像素 4 字节）
    // dst  : 输出图像像素数据（与 src 大小相同）
    // width, height : 图像尺寸
    void Grayscale(const unsigned char* src,
        unsigned char* dst,
        int width,
        int height)
    {
        // 图像总像素数量
        int pixels = width * height;

        // 逐像素处理
        for (int i = 0; i < pixels; ++i)
        {
            // 计算第 i 个像素在一维字节数组中的起始位置
            // 每个像素占 4 个字节：R G B A
            int idx = i * 4;

            // 读取原始颜色分量
            unsigned char r = src[idx + 0];  // Red
            unsigned char g = src[idx + 1];  // Green
            unsigned char b = src[idx + 2];  // Blue
            unsigned char a = src[idx + 3];  // Alpha (透明度)

            // 使用加权公式计算灰度值,这个公式为什么是对的 ,我也不知道,前人总结的 
            // 该公式符合人眼对亮度的感知特性
            unsigned char gray = static_cast<unsigned char>(
                0.299f * r +   // 红色权重
                0.587f * g +   // 绿色权重
                0.114f * b     // 蓝色权重
                );

            // 将灰度值写回 RGB 三个通道
            dst[idx + 0] = gray; // R
            dst[idx + 1] = gray; // G
            dst[idx + 2] = gray; // B

            // 保持原有透明度不变
            dst[idx + 3] = a;    // A
        }
    }

}
