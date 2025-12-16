using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace utils.Memento
{
    public class Editor
    {
        public string Content { get; private set; } = "";

        public void Type(string text)
        {
            Content += text;
        }

        public EditorMemento Save()
        {
            return new EditorMemento(Content);
        }

        public void Restore(EditorMemento memento)
        {
            Content = memento.Content;
        }
    }

}
