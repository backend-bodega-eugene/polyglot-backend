using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace utils.Proxy
{
    public class ProxyVideoService : IVideoService
    {
        private RealVideoService _realService;
        private Dictionary<string, string> _cache = new();

        public string GetVideo(string name)
        {
            if (_cache.ContainsKey(name))
            {
                Console.WriteLine($"Fetching '{name}' from cache.");
                return _cache[name];
            }

            Console.WriteLine($"'{name}' not found in cache, delegating to RealVideoService...");
            _realService ??= new RealVideoService();
            string video = _realService.GetVideo(name);
            _cache[name] = video;
            return video;
        }
    }

}
