using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace utils.Bridge
{
    public class Square : Shape
    {
        public Square(IColor color) : base(color) { }

        public override void Draw()
        {
            Console.WriteLine($"Drawing a square. {color.ApplyColor()}");
        }
    }
}
