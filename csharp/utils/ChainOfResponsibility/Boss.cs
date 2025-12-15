using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace utils.ChainOfResponsibility
{
    public class Boss : Approver
    {
        public override void HandleRequest(int days)
        {
            Console.WriteLine($"老板批准了 {days} 天假期（不批不行了）");
        }
    }

}
