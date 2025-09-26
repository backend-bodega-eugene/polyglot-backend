using System;
using System.Collections;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace utils.Factory
{
    internal sealed class EugeneIndex
    {
        private static readonly Lazy<EugeneIndex> _instance = new(() => new EugeneIndex());
        public static EugeneIndex Instance => _instance.Value;

        private readonly Dictionary<string, IEugene> _map;

        private EugeneIndex()
        {
            _map = new Dictionary<string, IEugene>(StringComparer.OrdinalIgnoreCase)
            {
                ["laohu"] = new EugeneLaohu()
            };
        }

        public IReadOnlyDictionary<string, IEugene> MyEugene() => _map;

        // 可选：暴露注册点，支持扩展而不改工厂
       // public bool Register(string key, IEugene impl) => _map.TryAdd(key, impl);
    }

}
