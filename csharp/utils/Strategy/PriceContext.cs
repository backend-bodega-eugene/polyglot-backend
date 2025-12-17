using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace utils.Strategy
{
    public class PriceContext
    {
        private IPriceStrategy _strategy;

        public PriceContext(IPriceStrategy strategy)
        {
            _strategy = strategy;
        }

        public void SetStrategy(IPriceStrategy strategy)
        {
            _strategy = strategy;
        }

        public decimal GetFinalPrice(decimal price)
        {
            return _strategy.Calculate(price);
        }
    }
    class Program
    {
        static void Main()
        {
            var context = new PriceContext(new NormalPriceStrategy());

            Console.WriteLine(context.GetFinalPrice(120)); // 120

            context.SetStrategy(new DiscountPriceStrategy());
            Console.WriteLine(context.GetFinalPrice(120)); // 96

            context.SetStrategy(new FullReductionStrategy());
            Console.WriteLine(context.GetFinalPrice(120)); // 100
        }
    }

}
