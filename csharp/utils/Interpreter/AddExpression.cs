using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace utils.Interpreter
{
    public class AddExpression : IExpression
    {
        private IExpression _left;
        private IExpression _right;

        public AddExpression(IExpression left, IExpression right)
        {
            _left = left;
            _right = right;
        }

        public int Interpret() => _left.Interpret() + _right.Interpret();
    }

    public class SubtractExpression : IExpression
    {
        private IExpression _left;
        private IExpression _right;

        public SubtractExpression(IExpression left, IExpression right)
        {
            _left = left;
            _right = right;
        }

        public int Interpret() => _left.Interpret() - _right.Interpret();
    }
    class Program
    {
        static void Main()
        {
            // 表达式：3 + 5 - 2
            IExpression expr = new SubtractExpression(
                new AddExpression(
                    new NumberExpression(3),
                    new NumberExpression(5)
                ),
                new NumberExpression(2)
            );

            Console.WriteLine($"结果: {expr.Interpret()}");
        }
    }

}
