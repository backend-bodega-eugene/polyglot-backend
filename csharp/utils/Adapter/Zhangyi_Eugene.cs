using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace utils.Adapter
{
    public class Zhangyi_Eugene:IEugene
    {
        private Zhangyi  zhangyi;
        public Zhangyi_Eugene(Zhangyi zhangyi)
        {
            this.zhangyi = zhangyi;

        }
        public string GetName()
        {
            return zhangyi.PrintName();
        }
    }
}
