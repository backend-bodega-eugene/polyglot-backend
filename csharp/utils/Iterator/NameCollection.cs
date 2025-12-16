using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace utils.Iterator
{
    public class NameCollection : IAggregate<string>
    {
        private List<string> _names = new List<string>
    {
        "Alice",
        "Bob",
        "Charlie"
    };

        public IIterator<string> CreateIterator()
        {
            return new NameIterator(this);
        }

        public int Count => _names.Count;

        public string GetItem(int index)
        {
            return _names[index];
        }
    }

}
