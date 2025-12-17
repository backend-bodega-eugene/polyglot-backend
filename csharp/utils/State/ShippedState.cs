using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace utils.State
{
    public class ShippedState : IOrderState
    {
        public void Handle(OrderContext context)
        {
            Console.WriteLine("订单已发货，交易完成。");
        }
    }

}
