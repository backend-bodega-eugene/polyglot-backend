using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace utils.Command
{
    // 接收者
    public class Light
    {
        public void On() => Console.WriteLine("灯开了 💡");
        public void Off() => Console.WriteLine("灯关了 🕯️");
    }

    // 具体命令
    public class LightOnCommand : ICommand
    {
        private Light _light;
        public LightOnCommand(Light light) => _light = light;

        public void Execute() => _light.On();
    }

    public class LightOffCommand : ICommand
    {
        private Light _light;
        public LightOffCommand(Light light) => _light = light;

        public void Execute() => _light.Off();
    }

}
