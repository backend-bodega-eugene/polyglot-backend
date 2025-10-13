using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace utils.Flyweight
{
    public class TreeType : ITree
    {
        private string name;
        private string color;
        private string texture;

        public TreeType(string name, string color, string texture)
        {
            this.name = name;
            this.color = color;
            this.texture = texture;
        }

        public void Display(int x, int y)
        {
            Console.WriteLine($"Tree [{name}, {color}, {texture}] at ({x}, {y})");
        }
    }

}
