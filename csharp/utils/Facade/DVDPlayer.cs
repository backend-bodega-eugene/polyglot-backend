using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace utils.Facade
{
    public class DVDPlayer
    {
        public void On() => Console.WriteLine("DVD Player is ON");
        public void Play(string movie) => Console.WriteLine($"Playing movie: {movie}");
        public void Off() => Console.WriteLine("DVD Player is OFF");
    }

    public class Projector
    {
        public void On() => Console.WriteLine("Projector is ON");
        public void WideScreenMode() => Console.WriteLine("Projector in widescreen mode");
        public void Off() => Console.WriteLine("Projector is OFF");
    }

    public class Amplifier
    {
        public void On() => Console.WriteLine("Amplifier is ON");
        public void SetVolume(int level) => Console.WriteLine($"Amplifier volume set to {level}");
        public void Off() => Console.WriteLine("Amplifier is OFF");
    }

    public class Lights
    {
        public void Dim(int level) => Console.WriteLine($"Lights dimmed to {level}%");
    }

}
