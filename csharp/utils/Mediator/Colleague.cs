using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace utils.Mediator
{
    public abstract class Colleague
    {
        protected IMediator mediator;

        protected Colleague(IMediator mediator)
        {
            this.mediator = mediator;
        }
    }

}
