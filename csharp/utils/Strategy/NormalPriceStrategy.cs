using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace utils.Strategy
{
    public class NormalPriceStrategy : IPriceStrategy
    {
        public decimal Calculate(decimal price)
            => price;
    }

}
