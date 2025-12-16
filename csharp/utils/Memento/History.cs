using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace utils.Memento
{
    public class History
    {
        private Stack<EditorMemento> _history = new();

        public void Push(EditorMemento memento)
        {
            _history.Push(memento);
        }

        public EditorMemento Pop()
        {
            return _history.Pop();
        }
    }

}
