using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace utils.Decorator
{
    // 装饰器：加入牛奶
    public class MilkDecorator : BeverageDecorator
    {
        public MilkDecorator(Beverage beverage) : base(beverage) { }

        public override double Cost()
        {
            return _beverage.Cost() + 1.5;  // 加牛奶的额外费用
        }
    }
}
