using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace utils.Flyweight
{
    public class TreeFactory
    {
        private static Dictionary<string, TreeType> _treeTypes = new();

        public static TreeType GetTreeType(string name, string color, string texture)
        {
            string key = $"{name}-{color}-{texture}";

            if (!_treeTypes.ContainsKey(key))
            {
                Console.WriteLine($"Creating new TreeType: {key}");
                _treeTypes[key] = new TreeType(name, color, texture);
            }
            else
            {
                Console.WriteLine($"Reusing existing TreeType: {key}");
            }

            return _treeTypes[key];
        }
    }

}
