using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace utils.Strategy
{
    public class DiscountPriceStrategy : IPriceStrategy
    {
        public decimal Calculate(decimal price)
            => price * 0.8m;
    }

}
