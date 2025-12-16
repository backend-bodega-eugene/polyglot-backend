using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace utils.Memento
{
    public class EditorMemento
    {
        public string Content { get; }

        public EditorMemento(string content)
        {
            Content = content;
        }
    }

}
