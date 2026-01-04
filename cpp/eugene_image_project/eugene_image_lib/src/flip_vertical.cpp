#include "flip_vertical.h"

namespace eugene
{
    void FlipVertical(const unsigned char* src,
        unsigned char* dst,
        int width,
        int height)
    {
        for (int y = 0; y < height; ++y)
        {
            for (int x = 0; x < width; ++x)
            {
                int srcIdx = (y * width + x) * 4;
                int dstIdx = ((height - 1 - y) * width + x) * 4;

                dst[dstIdx + 0] = src[srcIdx + 0];
                dst[dstIdx + 1] = src[srcIdx + 1];
                dst[dstIdx + 2] = src[srcIdx + 2];
                dst[dstIdx + 3] = src[srcIdx + 3];
            }
        }
    }
}
