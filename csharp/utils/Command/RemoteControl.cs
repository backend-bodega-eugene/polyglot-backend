using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace utils.Command
{
    public class RemoteControl
    {
        private ICommand _command;

        public void SetCommand(ICommand command)
        {
            _command = command;
        }

        public void PressButton()
        {
            _command?.Execute();
        }
    }
    class Program
    {
        static void Main()
        {
            var light = new Light();

            ICommand lightOn = new LightOnCommand(light);
            ICommand lightOff = new LightOffCommand(light);

            var remote = new RemoteControl();

            // 开灯
            remote.SetCommand(lightOn);
            remote.PressButton();

            // 关灯
            remote.SetCommand(lightOff);
            remote.PressButton();
        }
    }

}
