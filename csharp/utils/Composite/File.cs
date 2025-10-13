using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace utils.Composite
{
    // 文件类：Leaf节点
    public class File : FileSystemComponent
    {
        private string name;
        private int size;

        public File(string name, int size)
        {
            this.name = name;
            this.size = size;
        }

        public override void ShowDetails()
        {
            Console.WriteLine($"File: {name}, Size: {size}KB");
        }
    }

}
