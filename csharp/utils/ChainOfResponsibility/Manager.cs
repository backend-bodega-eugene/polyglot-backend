using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace utils.ChainOfResponsibility
{
    public class Manager : Approver
    {
        public override void HandleRequest(int days)
        {
            if (days <= 3)
            {
                Console.WriteLine($"经理批准了 {days} 天假期");
            }
            else
            {
                _next?.HandleRequest(days);
            }
        }
    }

}
