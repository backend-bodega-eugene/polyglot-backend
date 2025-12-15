using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace utils.Interpreter
{
    // 抽象表达式
    public interface IExpression
    {
        int Interpret();
    }

}
