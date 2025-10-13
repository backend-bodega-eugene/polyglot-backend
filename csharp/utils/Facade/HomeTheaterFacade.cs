using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace utils.Facade
{
    public class HomeTheaterFacade
    {
        private DVDPlayer dvd;
        private Projector projector;
        private Amplifier amp;
        private Lights lights;

        public HomeTheaterFacade(DVDPlayer dvd, Projector projector, Amplifier amp, Lights lights)
        {
            this.dvd = dvd;
            this.projector = projector;
            this.amp = amp;
            this.lights = lights;
        }

        public void WatchMovie(string movie)
        {
            Console.WriteLine("\nGet ready to watch a movie...");
            lights.Dim(10);
            projector.On();
            projector.WideScreenMode();
            amp.On();
            amp.SetVolume(5);
            dvd.On();
            dvd.Play(movie);
            Console.WriteLine("Movie started!\n");
        }

        public void EndMovie()
        {
            Console.WriteLine("\nShutting movie theater down...");
            lights.Dim(100);
            projector.Off();
            amp.Off();
            dvd.Off();
            Console.WriteLine("Movie ended!\n");
        }
    }

}
