#pragma once

namespace eugene
{
    // 图像二值化
    // threshold: 0~255，低于阈值为黑，高于为白
    void Binarize(
        const unsigned char* src,
        unsigned char* dst,
        int width,
        int height,
        unsigned char threshold
    );
}
