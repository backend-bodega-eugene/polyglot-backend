using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace utils.Factory
{
    class EugeneLaohu : IEugene
    {
        public string GetEugene(string uegene)
        {
            return $"我是老胡,我的名字叫{uegene}";
        }
    }

}
