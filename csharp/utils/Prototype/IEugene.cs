using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace utils.Prototype
{
    public interface IEugene
    {
         IEugene Clone();
        IEugene EugeneMemberwiseClone();
    }
}
