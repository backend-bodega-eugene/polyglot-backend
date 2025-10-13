using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace utils.Bridge
{
    public abstract class Shape
    {
        protected IColor color; // 持有实现部分的引用

        public Shape(IColor color)
        {
            this.color = color;
        }

        public abstract void Draw();
    }
}
