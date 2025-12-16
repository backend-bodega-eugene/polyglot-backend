using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace utils.Observer
{
    public class PhoneClient : IObserver
    {
        public void Update(string message)
        {
            Console.WriteLine($"📱 Phone 收到新闻：{message}");
        }
    }

    public class WebClient : IObserver
    {
        public void Update(string message)
        {
            Console.WriteLine($"💻 Web 收到新闻：{message}");
        }
    }

}
