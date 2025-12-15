using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace utils.ChainOfResponsibility
{
    public abstract class Approver
    {
        protected Approver _next;

        public void SetNext(Approver next)
        {
            _next = next;
        }

        public abstract void HandleRequest(int days);
    }
    class Program
    {
        static void Main()
        {
            var leader = new TeamLeader();
            var manager = new Manager();
            var boss = new Boss();

            leader.SetNext(manager);
            manager.SetNext(boss);

            leader.HandleRequest(1);
            leader.HandleRequest(3);
            leader.HandleRequest(7);
        }
    }
}
