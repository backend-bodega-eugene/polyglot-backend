using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace utils.Iterator
{
    public class NameIterator : IIterator<string>
    {
        private NameCollection _collection;
        private int _index = 0;

        public NameIterator(NameCollection collection)
        {
            _collection = collection;
        }

        public bool HasNext()
        {
            return _index < _collection.Count;
        }

        public string Next()
        {
            return _collection.GetItem(_index++);
        }
    }
    class Program
    {
        static void Main()
        {
            IAggregate<string> names = new NameCollection();
            IIterator<string> iterator = names.CreateIterator();

            while (iterator.HasNext())
            {
                Console.WriteLine(iterator.Next());
            }
        }
    }
}
