using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace utils.Strategy
{
    public interface IPriceStrategy
    {
        decimal Calculate(decimal price);
    }

}
