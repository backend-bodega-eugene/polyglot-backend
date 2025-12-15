using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace utils.ChainOfResponsibility
{
    public class TeamLeader : Approver
    {
        public override void HandleRequest(int days)
        {
            if (days <= 1)
            {
                Console.WriteLine($"组长批准了 {days} 天假期");
            }
            else
            {
                _next?.HandleRequest(days);
            }
        }
    }

}
