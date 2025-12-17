using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace utils.Visitor
{
    public class PdfFile : IFile
    {
        public void Accept(IFileVisitor visitor)
            => visitor.Visit(this);
    }

    public class WordFile : IFile
    {
        public void Accept(IFileVisitor visitor)
            => visitor.Visit(this);
    }

}
