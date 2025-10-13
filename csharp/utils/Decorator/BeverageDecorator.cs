using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace utils.Decorator
{
    // 装饰器基类
    public abstract class BeverageDecorator : Beverage
    {
        protected Beverage _beverage;  // 被装饰的饮品

        public BeverageDecorator(Beverage beverage)
        {
            _beverage = beverage;
        }

        public override double Cost()
        {
            return _beverage.Cost();
        }
    }

}
