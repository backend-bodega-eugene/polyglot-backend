using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace utils.Mediator
{
    public class Button : Colleague
    {
        public Button(IMediator mediator) : base(mediator) { }

        public void Click()
        {
            Console.WriteLine("Button clicked");
            mediator.Notify(this, "click");
        }
    }

    public class TextBox : Colleague
    {
        public TextBox(IMediator mediator) : base(mediator) { }

        public void Clear()
        {
            Console.WriteLine("TextBox cleared");
        }
    }

    public class Label : Colleague
    {
        public Label(IMediator mediator) : base(mediator) { }

        public void SetText(string text)
        {
            Console.WriteLine($"Label text set to: {text}");
        }
    }

}
