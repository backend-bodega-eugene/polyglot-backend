
#include "binarize.h"

namespace eugene
{
    void Binarize(const unsigned char* src,
        unsigned char* dst,
        int width,
        int height,
        unsigned char threshold)
    {
        int pixels = width * height;

        for (int i = 0; i < pixels; ++i)
        {
            int idx = i * 4;

            unsigned char r = src[idx + 0];
            unsigned char g = src[idx + 1];
            unsigned char b = src[idx + 2];
            unsigned char a = src[idx + 3];

            // 灰度化（先转成亮度）
            unsigned char gray = static_cast<unsigned char>(
                0.299f * r + 0.587f * g + 0.114f * b
                );

            // 二值化
            unsigned char value = (gray >= threshold) ? 255 : 0;

            dst[idx + 0] = value;
            dst[idx + 1] = value;
            dst[idx + 2] = value;
            dst[idx + 3] = a;   // 保留透明度
        }
    }
}
