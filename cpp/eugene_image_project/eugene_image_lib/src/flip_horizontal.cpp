#include "flip_horizontal.h"

namespace eugene
{
    // 水平翻转（左右镜像）
    // src: 输入图像像素数据（RGBA）
    // dst: 输出图像像素数据
    // width, height: 图像尺寸
    void FlipHorizontal(const unsigned char* src,
        unsigned char* dst,
        int width,
        int height)
    {
        for (int y = 0; y < height; ++y) // 遍历每一行
        {
            for (int x = 0; x < width; ++x) // 遍历当前行的每一列
            {
                // 计算原始像素在一维数组中的位置
                int srcIdx = (y * width + x) * 4;
                // 计算翻转后目标像素的位置（左右镜像）
                int dstIdx = (y * width + (width - 1 - x)) * 4;
                // 拷贝 RGBA 四个通道
                dst[dstIdx + 0] = src[srcIdx + 0];
                dst[dstIdx + 1] = src[srcIdx + 1];
                dst[dstIdx + 2] = src[srcIdx + 2];
                dst[dstIdx + 3] = src[srcIdx + 3];// A（保持透明度）
            }
        }
    }
}
