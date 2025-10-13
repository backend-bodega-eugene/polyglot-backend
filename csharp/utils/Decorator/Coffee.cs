using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace utils.Decorator
{
    // 具体组件：咖啡
    public class Coffee : Beverage
    {
        public override double Cost()
        {
            return 5.0;  // 咖啡的基本价格
        }
    }

}
