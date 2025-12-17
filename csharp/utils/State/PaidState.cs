using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace utils.State
{
    public class PaidState : IOrderState
    {
        public void Handle(OrderContext context)
        {
            Console.WriteLine("已支付，准备发货...");
            context.SetState(new ShippedState());
        }
    }

}
