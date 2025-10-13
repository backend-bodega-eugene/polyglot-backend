using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace utils.Decorator
{
    // 装饰器：加入糖
    public class SugarDecorator : BeverageDecorator
    {
        public SugarDecorator(Beverage beverage) : base(beverage) { }

        public override double Cost()
        {
            return _beverage.Cost() + 0.5;  // 加糖的额外费用
        }
    }
}
