using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace utils.Adapter
{
    public class Laohu_Eugene: IEugene
    {
        private Laohu laohu;
        public Laohu_Eugene(Laohu laohu) 
        {
            this.laohu = laohu;
        
        }
        public string GetName()
        {
            return laohu.PrintName();
        }
    }
}
