using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace utils.State
{
    public class UnpaidState : IOrderState
    {
        public void Handle(OrderContext context)
        {
            Console.WriteLine("订单未支付，正在等待付款...");
            context.SetState(new PaidState());
        }
    }

}
