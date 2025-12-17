using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace utils.Visitor
{
    public class PrintVisitor : IFileVisitor
    {
        public void Visit(PdfFile pdf)
        {
            Console.WriteLine("打印 PDF 文件");
        }

        public void Visit(WordFile word)
        {
            Console.WriteLine("打印 Word 文件");
        }
    }

}
