using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace utils.Visitor
{
    public interface IFile
    {
        void Accept(IFileVisitor visitor);
    }

}
