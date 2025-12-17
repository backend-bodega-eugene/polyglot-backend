using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace utils.State
{
    public class OrderContext
    {
        private IOrderState _state;

        public OrderContext(IOrderState state)
        {
            _state = state;
        }

        public void SetState(IOrderState state)
        {
            _state = state;
        }

        public void Request()
        {
            _state.Handle(this);
        }
    }
    class Program
    {
        static void Main()
        {
            var order = new OrderContext(new UnpaidState());

            order.Request(); // 未支付 → 已支付
            order.Request(); // 已支付 → 已发货
            order.Request(); // 已发货
        }
    }

}
