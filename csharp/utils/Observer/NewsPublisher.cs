using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace utils.Observer
{
    public class NewsPublisher : ISubject
    {
        private readonly List<IObserver> _observers = new();
        private string _news;

        public void Attach(IObserver observer)
        {
            _observers.Add(observer);
        }

        public void Detach(IObserver observer)
        {
            _observers.Remove(observer);
        }

        public void Publish(string news)
        {
            _news = news;
            Notify();
        }

        public void Notify()
        {
            foreach (var observer in _observers)
            {
                observer.Update(_news);
            }
        }
    }

}
