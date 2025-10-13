using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace utils.Proxy
{
    public class RealVideoService : IVideoService
    {
        public string GetVideo(string name)
        {
            Console.WriteLine($"Loading video '{name}' from remote server...");
            Thread.Sleep(2000); // 模拟网络延迟
            return $"[Video Content of {name}]";
        }
    }

}
