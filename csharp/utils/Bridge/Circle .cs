using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace utils.Bridge
{
    public class Circle : Shape
    {
        public Circle(IColor color) : base(color) { }

        public override void Draw()
        {
            Console.WriteLine($"Drawing a circle. {color.ApplyColor()}");
        }
    }
}
