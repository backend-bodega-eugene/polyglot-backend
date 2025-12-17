using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace utils.Strategy
{
    public class FullReductionStrategy : IPriceStrategy
    {
        public decimal Calculate(decimal price)
            => price >= 100 ? price - 20 : price;
    }

}
