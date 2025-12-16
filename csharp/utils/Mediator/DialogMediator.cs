using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace utils.Mediator
{
    public class DialogMediator : IMediator
    {
        public Button Button { get; set; }
        public TextBox TextBox { get; set; }
        public Label Label { get; set; }

        public void Notify(object sender, string ev)
        {
            if (sender == Button && ev == "click")
            {
                TextBox.Clear();
                Label.SetText("Clicked!");
            }
        }
    }

}
