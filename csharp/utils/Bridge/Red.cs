using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace utils.Bridge
{
    public class Red : IColor
    {
        public string ApplyColor()
        {
            return "Red color applied.";
        }
    }
}
