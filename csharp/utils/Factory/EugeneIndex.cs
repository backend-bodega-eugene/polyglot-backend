using System;
using System.Collections;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace utils.Factory
{
    internal class EugeneIndex
    {
        private EugeneIndex()
        {
        }
        private static EugeneIndex? instance;
        public static EugeneIndex Instance
        {
            get
            {
                if (instance == null)
                    instance = new EugeneIndex();

                return instance;
            }
        
        }
        private Dictionary<string, IEugene> myEugene;
        public Dictionary<string, IEugene> MyEugene()
        {
            if (myEugene == null)
            {
                myEugene = new Dictionary<string, IEugene>();
            }
            myEugene.Add("laohu", new EugeneLaohu());
            return myEugene;
        }

}  
}
